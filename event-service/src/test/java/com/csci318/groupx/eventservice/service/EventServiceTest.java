package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import com.csci318.groupx.eventservice.infrastructure.repository.EventRegistrationRepository;
import com.csci318.groupx.eventservice.infrastructure.repository.EventRepository;
import com.csci318.groupx.eventservice.presentation.dto.EventScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:event_service_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationService registrationService;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {

        eventRepository.deleteAll();

        eventRepository.save(new Event(
                "Sydney AI Conference",
                "Artificial intelligence conference and technology showcase",
                "Technology",
                "Sydney",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 18, 0),
                20.0,
                200,
                160
        ));

        eventRepository.save(new Event(
                "Sydney Tech Expo",
                "Technology and innovation exhibition",
                "Technology",
                "Sydney",
                LocalDateTime.of(2026, 9, 27, 10, 0),
                LocalDateTime.of(2026, 9, 27, 18, 0),
                60.0,
                300,
                90
        ));

        eventRepository.save(new Event(
                "Melbourne Music Festival",
                "Live music and entertainment festival",
                "Music",
                "Melbourne",
                LocalDateTime.of(2026, 9, 28, 12, 0),
                LocalDateTime.of(2026, 9, 28, 22, 0),
                90.0,
                500,
                450
        ));
    }

    @Test
    void shouldCombineFilterAndFuzzySearch() {

        List<Event> results =
                eventService.searchEvents(
                        "technlogy",
                        "Technology",
                        "Sydney",
                        30.0
                );

        assertEquals(1, results.size());
        assertEquals(
                "Sydney AI Conference",
                results.getFirst().getName()
        );
    }

    @Test
    void shouldReturnNoResultForUnrelatedSearch() {

        List<Event> results =
                eventService.searchEvents(
                        "football",
                        null,
                        null,
                        null
                );

        assertTrue(results.isEmpty());
    }

    @Test
    void shouldRankEventsByFinalScoreDescending() {

        List<EventScoreResponse> results =
                eventService.rankEvents(
                        "technlogy",
                        null,
                        null,
                        null
                );

        assertEquals(3, results.size());

        assertTrue(
                results.get(0).getFinalScore()
                        >= results.get(1).getFinalScore()
        );

        assertTrue(
                results.get(1).getFinalScore()
                        >= results.get(2).getFinalScore()
        );

        assertEquals(
                "Sydney AI Conference",
                results.getFirst().getEvent().getName()
        );
    }

    @Test
    void shouldPersistEventCreationUpdatesAndDeletion() {
        Event event = new Event(
                "Persistent event",
                "Initial description",
                "Community",
                "Sydney",
                LocalDateTime.of(2026, 10, 20, 12, 0),
                null,
                15.0,
                100,
                0
        );

        Event saved = eventService.createEvent(event);
        Long id = saved.getId();
        assertTrue(eventRepository.findById(id).isPresent());

        Event update = new Event(
                "Updated event",
                "Updated description",
                "Workshop",
                "Wollongong",
                LocalDateTime.of(2026, 10, 21, 12, 0),
                null,
                25.0,
                150,
                0
        );
        eventService.updateEvent(id, update);

        entityManager.clear();
        Event reloaded = eventRepository.findById(id).orElseThrow();
        assertEquals("Updated event", reloaded.getName());
        assertEquals("Updated description", reloaded.getDescription());
        assertEquals("Workshop", reloaded.getCategory());

        eventService.deleteEvent(id);
        entityManager.clear();
        assertTrue(eventRepository.findById(id).isEmpty());
    }

    @Test
    void shouldPersistAndCancelAccountEventRegistrations() {
        Event event = eventService.createEvent(new Event(
                "Registration test event",
                "Test registration persistence",
                "Community",
                "Sydney",
                LocalDateTime.of(2026, 11, 10, 12, 0),
                null,
                0.0,
                20,
                0
        ));
        Long accountId = 9001L;

        var first = registrationService.register(accountId, event.getId());
        var duplicate = registrationService.register(accountId, event.getId());
        assertEquals(first.getId(), duplicate.getId());
        assertEquals(1, registrationRepository.findByAccountId(accountId).size());
        assertEquals(1, eventRepository.findById(event.getId()).orElseThrow().getRegisteredCount());

        entityManager.clear();
        var persisted = registrationRepository.findByAccountId(accountId);
        assertEquals(1, persisted.size());

        registrationService.cancel(persisted.getFirst().getId(), accountId);
        entityManager.clear();
        assertTrue(registrationRepository.findByAccountId(accountId).isEmpty());
        assertEquals(0, eventRepository.findById(event.getId()).orElseThrow().getRegisteredCount());
    }
}