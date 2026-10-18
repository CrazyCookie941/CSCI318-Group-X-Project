package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class NotificationDispatchClient {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationDispatchClient.class);
    private static final RestClient CLIENT = RestClient.create("http://localhost:8084");

    public void eventCreated(Event event) {
        try {
            CLIENT.post()
                    .uri("/api/notifications/events/created")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NewEventRequest(event.getId(), event.getName(), event.getLocation()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            logger.error("Could not dispatch new-event notifications for eventId={}",
                    event.getId(), exception);
        }
    }

    public void eventChanged(Event event, String status, List<Long> accountIds) {
        String title = switch (status) {
            case "POSTPONED" -> "Event postponed";
            case "CANCELLED" -> "Event cancelled";
            case "REFUNDED" -> "Event refunded";
            default -> throw new IllegalArgumentException("Unsupported notification status: " + status);
        };
        String message = switch (status) {
            case "POSTPONED" -> event.getName() + " has been postponed. Check the event listing for updates.";
            case "CANCELLED" -> event.getName() + " has been cancelled.";
            case "REFUNDED" -> "A refund has been issued for " + event.getName() + ".";
            default -> throw new IllegalArgumentException("Unsupported notification status: " + status);
        };
        try {
            CLIENT.post()
                    .uri("/api/notifications/events/changed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new EventChangeRequest(event.getId(), title, message, accountIds))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            logger.error("Could not dispatch event-change notifications for eventId={} status={}",
                    event.getId(), status, exception);
        }
    }

    /** Required notification for cancellation: unlike eventChanged(), failures are surfaced. */
    public void eventCancelledRequired(Event event, List<Long> accountIds) {
        List<Long> uniqueRecipients = accountIds.stream()
                .filter(id -> id != null && id > 0).distinct().toList();
        if (uniqueRecipients.isEmpty()) {
            return;
        }
        try {
            Integer created = CLIENT.post()
                    .uri("/api/notifications/events/changed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new EventChangeRequest(event.getId(), "Event cancelled",
                            event.getName() + " has been cancelled.", uniqueRecipients))
                    .retrieve().body(Integer.class);
            if (created == null || created < uniqueRecipients.size()) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                        "Not all registered users received cancellation notifications.");
            }
        } catch (org.springframework.web.client.RestClientException ex) {
            logger.error("Cancellation notification dispatch failed for eventId={}", event.getId(), ex);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "Cancellation notifications could not be saved; retry the cancellation.", ex);
        }
    }
    private record NewEventRequest(Long eventId, String title, String location) {
    }

    private record EventChangeRequest(
            Long eventId,
            String title,
            String message,
            List<Long> accountIds
    ) {
    }
}
