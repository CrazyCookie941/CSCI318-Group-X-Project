package com.csci318.groupx.notificationservice.messaging;

public record AccountEvent(
        int schemaVersion,
        String eventId,
        String eventType,
        Long accountId,
        String occurredAt
) {
}
