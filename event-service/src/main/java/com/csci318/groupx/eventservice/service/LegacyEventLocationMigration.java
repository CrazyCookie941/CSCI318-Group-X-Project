package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import com.csci318.groupx.eventservice.infrastructure.repository.EventRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Locale;

@Component
public class LegacyEventLocationMigration implements ApplicationRunner {
    private static final Map<String, Coordinates> KNOWN_LOCATIONS = Map.of(
            "afterpay arena", new Coordinates(-33.8442890, 151.0622385),
            "wollongong central", new Coordinates(-34.4245774, 150.8941979),
            "sydney central park", new Coordinates(-33.8846058, 151.2007392),
            "wollongong community hall", new Coordinates(-34.4259309, 150.8976923));

    private final EventRepository eventRepository;

    public LegacyEventLocationMigration(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        eventRepository.findAll().forEach(event -> {
            if (event.getLocation() == null || event.getLatitude() != null && event.getLongitude() != null) {
                return;
            }
            String location = event.getLocation().trim().toLowerCase(Locale.ROOT);
            Coordinates coordinates = KNOWN_LOCATIONS.entrySet().stream()
                    .filter(entry -> location.equals(entry.getKey()) || location.startsWith(entry.getKey() + ","))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (coordinates != null) {
                event.setLatitude(coordinates.latitude());
                event.setLongitude(coordinates.longitude());
                eventRepository.save(event);
            }
        });
    }

    private record Coordinates(double latitude, double longitude) {
    }
}
