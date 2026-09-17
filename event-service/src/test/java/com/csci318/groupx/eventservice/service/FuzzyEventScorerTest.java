package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FuzzyEventScorerTest {

    private final FuzzyMatcher fuzzyMatcher = new FuzzyMatcher();
    private final FuzzyEventScorer fuzzyEventScorer =
            new FuzzyEventScorer(fuzzyMatcher);

    @Test
    void shouldReturnFullAffordabilityForCheapEvent() {
        double score =
                fuzzyEventScorer.calculateAffordabilityScore(20.0);

        assertEquals(1.0, score);
    }

    @Test
    void shouldReturnHalfAffordabilityForMidPriceEvent() {
        double score =
                fuzzyEventScorer.calculateAffordabilityScore(60.0);

        assertEquals(0.5, score);
    }

    @Test
    void shouldReturnZeroAffordabilityForExpensiveEvent() {
        double score =
                fuzzyEventScorer.calculateAffordabilityScore(100.0);

        assertEquals(0.0, score);
    }

    @Test
    void shouldCalculatePopularityFromRegistrationRatio() {
        double score =
                fuzzyEventScorer.calculatePopularityScore(
                        160,
                        200
                );

        assertEquals(0.8, score, 0.0001);
    }

    @Test
    void shouldCapPopularityAtOne() {
        double score =
                fuzzyEventScorer.calculatePopularityScore(
                        250,
                        200
                );

        assertEquals(1.0, score);
    }

    @Test
    void shouldCalculateCorrectFinalScore() {

        Event event = new Event(
                "Sydney AI Conference",
                "Artificial intelligence conference",
                "Technology",
                "Sydney",
                LocalDateTime.of(2026, 9, 25, 10, 0),
                LocalDateTime.of(2026, 9, 25, 18, 0),
                20.0,
                200,
                160
        );

        double score =
                fuzzyEventScorer.calculateFinalScore(
                        "technlogy",
                        event
                );

        assertEquals(0.91, score, 0.0001);
    }
}