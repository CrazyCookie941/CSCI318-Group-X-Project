package com.csci318.groupx.accountservice.messaging;

/**
 * Public integration event. Intentionally excludes email, username,
 * passwords, password hashes and other account profile data.
 */
public record AccountEvent(
        int schemaVersion,
        String eventId,
        String eventType,
        Long accountId,
        String occurredAt
) { }
