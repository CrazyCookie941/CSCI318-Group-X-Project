package com.csci318.groupx.registrationservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

// Talks to Michael's event-service. Nothing in the service itself is changed.
final class EventServiceClient {

    // Mirrors the Event the service sends and receives
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ServiceEvent(
            Long id,
            String name,
            String description,
            String category,
            String location,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Double price,
            Integer capacity,
            Integer registeredCount,
            Long ownerId,
            String imageLocation,
            Double latitude,
            Double longitude
    ) {
    }

    private static final RestClient CLIENT = RestClient.create("http://localhost:8081");

    private EventServiceClient() {
    }

    static List<ServiceEvent> getAll() {
        List<ServiceEvent> events = CLIENT.get()
                .uri("/api/events")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ServiceEvent>>() {
                });
        return events == null ? List.of() : events;
    }

    static ServiceEvent create(ServiceEvent event) {
        return CLIENT.post()
                .uri("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .body(ServiceEvent.class);
    }

    static ServiceEvent update(long id, ServiceEvent event) {
        return CLIENT.put()
                .uri("/api/events/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(event)
                .retrieve()
                .body(ServiceEvent.class);
    }

    static void delete(long id) {
        CLIENT.delete()
                .uri("/api/events/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    // Quick standalone test: list, create, delete, list again (leaves the service as it found it)
    public static void main(String[] args) {
        System.out.println("Events before: " + getAll());
        ServiceEvent created = create(new ServiceEvent(null, "Client test", "Made by EventServiceClient",
                "Concert", "Sydney", LocalDateTime.now().plusDays(7), null, 25.0, 100, 0,
                2L, "/Images/Bingo_night.jpg", null, null));
        System.out.println("Created: " + created);
        delete(created.id());
        System.out.println("Events after: " + getAll());
    }
}