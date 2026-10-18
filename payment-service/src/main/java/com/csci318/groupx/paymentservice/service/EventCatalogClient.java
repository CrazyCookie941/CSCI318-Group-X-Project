package com.csci318.groupx.paymentservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class EventCatalogClient {

    private final RestClient client;

    public EventCatalogClient(
            @Value("${payment.event-service-url:http://localhost:8081}")
            String baseUrl
    ) {
        this.client = RestClient.create(baseUrl);
    }

    public record EventSnapshot(
            Long id,
            String name,
            BigDecimal price
    ) {
    }

    public EventSnapshot requireEvent(Long eventId) {

        final List<Map<String, Object>> events;

        try {
            events = client.get()
                    .uri("/api/events")
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    List<Map<String, Object>>>() {}
                    );

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Event Service is unavailable. Payment was not recorded.",
                    ex
            );
        }

        if (events == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Event Service returned no data."
            );
        }

        for (Map<String, Object> event : events) {

            if (event == null || event.get("id") == null) {
                continue;
            }

            Long id;

            try {
                id = Long.valueOf(event.get("id").toString());
            } catch (NumberFormatException ex) {
                continue;
            }

            if (!eventId.equals(id)) {
                continue;
            }

            Object rawPrice = event.get("price");
            BigDecimal price;

            try {
                price = rawPrice == null
                        ? null
                        : new BigDecimal(rawPrice.toString());
            } catch (NumberFormatException ex) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Event has an invalid ticket price."
                );
            }

            Object rawName = event.get("name");

            String name = rawName == null
                    ? "Event #" + eventId
                    : rawName.toString();

            return new EventSnapshot(id, name, price);
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Selected event was not found."
        );
    }
}