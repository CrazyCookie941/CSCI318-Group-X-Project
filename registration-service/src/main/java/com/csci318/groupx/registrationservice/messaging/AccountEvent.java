package com.csci318.groupx.registrationservice.messaging;

public record AccountEvent(
        String eventType,
        Long accountId,
        String username,
        String email,
        String occurredAt
) {
}
