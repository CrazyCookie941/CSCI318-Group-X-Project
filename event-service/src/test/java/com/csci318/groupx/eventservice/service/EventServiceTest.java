package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import com.csci318.groupx.eventservice.infrastructure.repository.EventRepository;
import com.csci318.groupx.eventservice.presentation.dto.EventScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

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
}