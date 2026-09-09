package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import org.springframework.stereotype.Component;

@Component
public class FuzzyEventScorer {

    private final FuzzyMatcher fuzzyMatcher;

    public FuzzyEventScorer(FuzzyMatcher fuzzyMatcher) {
        this.fuzzyMatcher = fuzzyMatcher;
    }

    public double calculateRelevanceScore(String query, Event event) {

        if (query == null || query.isBlank()) {
            return 1.0;
        }

        double nameScore =
                fuzzyMatcher.similarity(query, event.getName());

        double categoryScore =
                fuzzyMatcher.similarity(query, event.getCategory());

        double locationScore =
                fuzzyMatcher.similarity(query, event.getLocation());

        return Math.max(
                nameScore,
                Math.max(categoryScore, locationScore)
        );
    }

    public double calculateAffordabilityScore(Double price) {

        if (price == null) {
            return 0.0;
        }

        if (price <= 20.0) {
            return 1.0;
        }

        if (price >= 100.0) {
            return 0.0;
        }

        return (100.0 - price) / 80.0;
    }

    public double calculatePopularityScore(
            Integer registeredCount,
            Integer capacity) {

        if (registeredCount == null ||
                capacity == null ||
                capacity <= 0) {

            return 0.0;
        }

        double score =
                (double) registeredCount / capacity;

        return Math.min(score, 1.0);
    }

    public double calculateFinalScore(
            String query,
            Event event) {

        double relevance =
                calculateRelevanceScore(query, event);

        double affordability =
                calculateAffordabilityScore(event.getPrice());

        double popularity =
                calculatePopularityScore(
                        event.getRegisteredCount(),
                        event.getCapacity()
                );

        return 0.5 * relevance
                + 0.3 * affordability
                + 0.2 * popularity;
    }
}