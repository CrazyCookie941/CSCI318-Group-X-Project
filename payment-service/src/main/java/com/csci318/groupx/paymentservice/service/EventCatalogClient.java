package com.csci318.groupx.paymentservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class EventCatalogClient {
    private final RestClient client;

    public EventCatalogClient(
            @Value("${payment.event-service-url:http://localhost:8081}") String baseUrl) {
        client = RestClient.create(baseUrl);
    }

    public record EventSnapshot(
            Long id, String name, BigDecimal price,
            String status, Integer capacity, Integer registeredCount) {
    }

    public EventSnapshot requireEvent(Long eventId) {
        if (eventId == null || eventId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid eventId required.");
        }
        final Map<String, Object> event;
        try {
            event = client.get().uri("/api/events/{id}", eventId).retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found.", ex);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Event Service could not be queried.", ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Event Service unavailable. No payment was recorded.", ex);
        }
        if (event == null || event.get("id") == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Event Service returned invalid data.");
        }
        try {
            Object rawPrice = event.get("price");
            BigDecimal price = rawPrice == null ? null : new BigDecimal(rawPrice.toString());
            String name = event.get("name") == null ? "Event #" + eventId : event.get("name").toString();
            String status = event.get("status") == null ? "" : event.get("status").toString();
            Integer capacity = asInteger(event.get("capacity"));
            Integer registered = asInteger(event.get("registeredCount"));
            return new EventSnapshot(eventId, name, price, status, capacity, registered);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event returned invalid price/capacity data.", ex);
        }
    }

    public boolean isRegisteredForEvent(Long accountId, Long eventId) {
        final List<Map<String, Object>> registrations;
        try {
            registrations = client.get()
                    .uri("/api/registrations?accountId={accountId}", accountId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Event registrations could not be checked. Payment was not recorded.", ex);
        }
        if (registrations == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Event Service returned no registration data.");
        }
        return registrations.stream().anyMatch(registration ->
                registration != null && eventId.toString().equals(
                        String.valueOf(registration.get("eventId"))));
    }
    private static Integer asInteger(Object value) {
        return value == null ? null : Integer.valueOf(value.toString());
    }
}
