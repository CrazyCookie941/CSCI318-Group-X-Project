package com.csci318.groupx.registrationservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class LocationGeocoder {
    private static final long MIN_REQUEST_INTERVAL_MS = 1_000;
    private static final RestClient CLIENT = createClient();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Map<String, List<GeocodedLocation>> CACHE = new HashMap<>();
    private static long nextRequestAt;

    private LocationGeocoder() {
    }

    private static RestClient createClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "CSCI318-Group-X-Project/1.0")
                .requestFactory(requestFactory)
                .build();
    }

    static synchronized List<GeocodedLocation> search(String query) {
        String key = query.trim().toLowerCase(Locale.ROOT);
        List<GeocodedLocation> cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        long waitMs = nextRequestAt - System.currentTimeMillis();
        if (waitMs > 0) {
            try {
                Thread.sleep(waitMs);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Location search was interrupted.", exception);
            }
        }
        nextRequestAt = System.currentTimeMillis() + MIN_REQUEST_INTERVAL_MS;

        String response = CLIENT.get()
                .uri(uriBuilder -> uriBuilder.path("/search")
                        .queryParam("q", query.trim())
                        .queryParam("format", "jsonv2")
                        .queryParam("limit", 5)
                        .build())
                .retrieve()
                .body(String.class);
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("The location service returned an empty response.");
        }

        JsonNode results;
        try {
            results = JSON.readTree(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("The location service returned invalid JSON.", exception);
        }
        if (!results.isArray()) {
            throw new IllegalStateException("The location service returned an unexpected response.");
        }

        List<GeocodedLocation> locations = java.util.stream.StreamSupport.stream(results.spliterator(), false)
                .map(result -> new GeocodedLocation(
                        result.path("display_name").asText(),
                        parseCoordinate(result, "lat"),
                        parseCoordinate(result, "lon")))
                .toList();
        CACHE.put(key, locations);
        return locations;
    }

    private static double parseCoordinate(JsonNode result, String field) {
        String value = result.path(field).asText();
        try {
            double coordinate = Double.parseDouble(value);
            double maximum = field.equals("lat") ? 90 : 180;
            if (!Double.isFinite(coordinate) || coordinate < -maximum || coordinate > maximum) {
                throw new NumberFormatException("Coordinate is outside its valid range");
            }
            return coordinate;
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("The location service returned an invalid coordinate.", exception);
        }
    }

    record GeocodedLocation(String displayName, double latitude, double longitude) {
        @Override
        public String toString() {
            return displayName;
        }
    }
}
