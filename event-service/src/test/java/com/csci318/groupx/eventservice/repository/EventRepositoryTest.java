package com.csci318.groupx.eventservice.infrastructure.repository;

import com.csci318.groupx.eventservice.domain.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {

        eventRepository.deleteAll();

        Event event1 = new Event(
                "Sydney Tech Expo",
                "Technology and innovation exhibition",
                "Technology",
                "Sydney",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 18, 0),
                25.0,
                300,
                80
        );

        Event event2 = new Event(
                "Melbourne Music Festival",
                "Live music and entertainment festival",
                "Music",
                "Melbourne",
                LocalDateTime.of(2026, 9, 28, 12, 0),
                LocalDateTime.of(2026, 9, 28, 22, 0),
                80.0,
                500,
                240
        );

        Event event3 = new Event(
                "Sydney Business Forum",
                "Business networking event",
                "Business",
                "Sydney",
                LocalDateTime.of(2026, 10, 2, 9, 0),
                LocalDateTime.of(2026, 10, 2, 16, 0),
                45.0,
                150,
                60
        );

        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
    }

    @Test
    void shouldFilterByCategory() {

        List<Event> results =
                eventRepository.filterEvents(
                        "Technology",
                        null,
                        null
                );

        assertEquals(1, results.size());
        assertEquals("Sydney Tech Expo", results.getFirst().getName());
    }

    @Test
    void shouldFilterByLocation() {

        List<Event> results =
                eventRepository.filterEvents(
                        null,
                        "Sydney",
                        null
                );

        assertEquals(2, results.size());
    }

    @Test
    void shouldFilterByMaxPrice() {

        List<Event> results =
                eventRepository.filterEvents(
                        null,
                        null,
                        30.0
                );

        assertEquals(1, results.size());
        assertEquals("Sydney Tech Expo", results.getFirst().getName());
    }

    @Test
    void shouldFilterByCombinedConditions() {

        List<Event> results =
                eventRepository.filterEvents(
                        "Technology",
                        "Sydney",
                        30.0
                );

        assertEquals(1, results.size());
        assertEquals("Sydney Tech Expo", results.getFirst().getName());
    }

    @Test
    void shouldReturnAllEventsWhenNoFiltersProvided() {

        List<Event> results =
                eventRepository.filterEvents(
                        null,
                        null,
                        null
                );

        assertEquals(3, results.size());
    }
}