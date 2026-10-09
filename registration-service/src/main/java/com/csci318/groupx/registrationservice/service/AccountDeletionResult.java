package com.csci318.groupx.registrationservice.service;

public record AccountDeletionResult(
        boolean success,
        String message,
        Status status
) {
    public enum Status {
        DELETED,
        BAD_REQUEST,
        NOT_FOUND,
        INVALID_PASSWORD,
        HAS_EVENTS,
        EVENT_SERVICE_UNAVAILABLE,
        RELATED_RECORDS
    }
}
