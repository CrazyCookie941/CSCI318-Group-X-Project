package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import com.csci318.groupx.eventservice.infrastructure.repository.EventRepository;
import com.csci318.groupx.eventservice.presentation.dto.EventScoreResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final FuzzyMatcher fuzzyMatcher;
    private final FuzzyEventScorer fuzzyEventScorer;

    public EventService(
            EventRepository eventRepository,
            FuzzyMatcher fuzzyMatcher,
            FuzzyEventScorer fuzzyEventScorer) {

        this.eventRepository = eventRepository;
        this.fuzzyMatcher = fuzzyMatcher;
        this.fuzzyEventScorer = fuzzyEventScorer;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Event createEvent(Event event) {
        validatePrice(event.getPrice());
        if (event.getStatus() == null || event.getStatus().isBlank()) {
            event.setStatus("SCHEDULED");
        }
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        validatePrice(updatedEvent.getPrice());
        return eventRepository.findById(id)
                .map(existingEvent -> {

                    existingEvent.setName(updatedEvent.getName());
                    existingEvent.setDescription(updatedEvent.getDescription());
                    existingEvent.setCategory(updatedEvent.getCategory());
                    existingEvent.setLocation(updatedEvent.getLocation());
                    existingEvent.setStartDateTime(updatedEvent.getStartDateTime());
                    existingEvent.setEndDateTime(updatedEvent.getEndDateTime());
                    existingEvent.setPrice(updatedEvent.getPrice());
                    existingEvent.setCapacity(updatedEvent.getCapacity());
                    existingEvent.setRegisteredCount(updatedEvent.getRegisteredCount());
                    existingEvent.setOwnerId(updatedEvent.getOwnerId());
                    existingEvent.setImageLocation(updatedEvent.getImageLocation());
                    existingEvent.setLatitude(updatedEvent.getLatitude());
                    existingEvent.setLongitude(updatedEvent.getLongitude());
                    if (updatedEvent.getStatus() != null && !updatedEvent.getStatus().isBlank()) {
                        existingEvent.setStatus(updatedEvent.getStatus());
                    }

                    return eventRepository.save(existingEvent);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    private static void validatePrice(Double price) {
        if (price == null || !Double.isFinite(price) || price < 0 || price > 999999.99) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Event price must be between 0 and 999999.99."
            );
        }
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    public List<Event> filterEvents(
            String category,
            String location,
            Double maxPrice) {

        return eventRepository.filterEvents(
                category,
                location,
                maxPrice
        );
    }

    public List<Event> fuzzySearch(String query) {

        return eventRepository.findAll()
                .stream()
                .filter(event -> {

                    double nameScore =
                            fuzzyMatcher.similarity(
                                    query,
                                    event.getName()
                            );

                    double categoryScore =
                            fuzzyMatcher.similarity(
                                    query,
                                    event.getCategory()
                            );

                    double locationScore =
                            fuzzyMatcher.similarity(
                                    query,
                                    event.getLocation()
                            );

                    double bestScore = Math.max(
                            nameScore,
                            Math.max(
                                    categoryScore,
                                    locationScore
                            )
                    );

                    return bestScore >= 0.5;
                })
                .toList();
    }

    public List<Event> searchEvents(
            String query,
            String category,
            String location,
            Double maxPrice) {

        List<Event> filteredEvents =
                eventRepository.filterEvents(
                        category,
                        location,
                        maxPrice
                );

        if (query == null || query.isBlank()) {
            return filteredEvents;
        }

        return filteredEvents
                .stream()
                .filter(event -> {

                    double nameScore =
                            fuzzyMatcher.similarity(
                                    query,
                                    event.getName()
                            );

                    double categoryScore =
                            fuzzyMatcher.similarity(
                                    query,
                                    event.getCategory()
                            );

                    double locationScore =
                            fuzzyMatcher.similarity(
                                    query,
                                    event.getLocation()
                            );

                    double bestScore = Math.max(
                            nameScore,
                            Math.max(
                                    categoryScore,
                                    locationScore
                            )
                    );

                    return bestScore >= 0.5;
                })
                .toList();
    }

    public List<EventScoreResponse> rankEvents(
            String query,
            String category,
            String location,
            Double maxPrice) {

        List<Event> filteredEvents =
                eventRepository.filterEvents(
                        category,
                        location,
                        maxPrice
                );

        return filteredEvents
                .stream()
                .map(event -> {

                    double relevanceScore =
                            fuzzyEventScorer.calculateRelevanceScore(
                                    query,
                                    event
                            );

                    double affordabilityScore =
                            fuzzyEventScorer.calculateAffordabilityScore(
                                    event.getPrice()
                            );

                    double popularityScore =
                            fuzzyEventScorer.calculatePopularityScore(
                                    event.getRegisteredCount(),
                                    event.getCapacity()
                            );

                    double finalScore =
                            fuzzyEventScorer.calculateFinalScore(
                                    query,
                                    event
                            );

                    return new EventScoreResponse(
                            event,
                            relevanceScore,
                            affordabilityScore,
                            popularityScore,
                            finalScore
                    );
                })
                .sorted(
                        Comparator.comparingDouble(
                                EventScoreResponse::getFinalScore
                        ).reversed()
                )
                .toList();
    }
}