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
