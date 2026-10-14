package com.csci318.groupx.eventservice.presentation.dto;

public record ChatResponse(
        String reply,
        String model,
        int eventsConsidered
) {
}