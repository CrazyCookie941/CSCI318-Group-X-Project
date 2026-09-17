package com.csci318.groupx.eventservice.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuzzyMatcherTest {

    private final FuzzyMatcher fuzzyMatcher = new FuzzyMatcher();

    @Test
    void shouldReturnOneForIdenticalStrings() {
        double score =
                fuzzyMatcher.similarity(
                        "Technology",
                        "Technology"
                );

        assertEquals(1.0, score);
    }

    @Test
    void shouldMatchMinorSpellingMistake() {
        double score =
                fuzzyMatcher.similarity(
                        "technlogy",
                        "Technology"
                );

        assertTrue(score >= 0.5);
    }

    @Test
    void shouldRejectUnrelatedWords() {
        double score =
                fuzzyMatcher.similarity(
                        "football",
                        "Technology"
                );

        assertTrue(score < 0.5);
    }

    @Test
    void shouldIgnoreCase() {
        double score =
                fuzzyMatcher.similarity(
                        "TECHNOLOGY",
                        "technology"
                );

        assertEquals(1.0, score);
    }

    @Test
    void shouldReturnZeroForNullInput() {
        double score =
                fuzzyMatcher.similarity(
                        null,
                        "Technology"
                );

        assertEquals(0.0, score);
    }

    @Test
    void shouldReturnZeroForEmptyStringAgainstNonEmptyString() {
        double score =
                fuzzyMatcher.similarity(
                        "",
                        "Technology"
                );

        assertEquals(0.0, score);
    }
}