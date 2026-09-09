package com.csci318.groupx.eventservice.presentation.dto;

import com.csci318.groupx.eventservice.domain.model.Event;

public class EventScoreResponse {

    private Event event;
    private double relevanceScore;
    private double affordabilityScore;
    private double popularityScore;
    private double finalScore;

    public EventScoreResponse(
            Event event,
            double relevanceScore,
            double affordabilityScore,
            double popularityScore,
            double finalScore) {

        this.event = event;
        this.relevanceScore = relevanceScore;
        this.affordabilityScore = affordabilityScore;
        this.popularityScore = popularityScore;
        this.finalScore = finalScore;
    }

    public Event getEvent() {
        return event;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public double getAffordabilityScore() {
        return affordabilityScore;
    }

    public double getPopularityScore() {
        return popularityScore;
    }

    public double getFinalScore() {
        return finalScore;
    }
}