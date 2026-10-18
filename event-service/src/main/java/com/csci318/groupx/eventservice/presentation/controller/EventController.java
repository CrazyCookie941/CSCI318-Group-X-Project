package com.csci318.groupx.eventservice.presentation.controller;

import com.csci318.groupx.eventservice.domain.model.Event;
import com.csci318.groupx.eventservice.presentation.dto.EventScoreResponse;
import com.csci318.groupx.eventservice.service.EventRegistrationService;
import com.csci318.groupx.eventservice.service.NotificationDispatchClient;
import com.csci318.groupx.eventservice.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventRegistrationService registrationService;
    private final NotificationDispatchClient notificationDispatchClient;

    public EventController(
            EventService eventService,
            EventRegistrationService registrationService,
            NotificationDispatchClient notificationDispatchClient
    ) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.notificationDispatchClient = notificationDispatchClient;
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/filter")
    public List<Event> filterEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double maxPrice) {

        return eventService.filterEvents(
                category,
                location,
                maxPrice
        );
    }

    @GetMapping("/search")
    public List<Event> searchEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double maxPrice) {

        return eventService.searchEvents(
                q,
                category,
                location,
                maxPrice
        );
    }

    @GetMapping("/rank")
    public List<EventScoreResponse> rankEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double maxPrice) {

        return eventService.rankEvents(
                q,
                category,
                location,
                maxPrice
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        Event created = eventService.createEvent(event);
        notificationDispatchClient.eventCreated(created);
        return created;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestBody Event event) {

        try {
            Event existing = eventService.getEventById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            Event updated = eventService.updateEvent(id, event);
            String status = updated.getStatus();
            String previousStatus = existing.getStatus() == null ? "SCHEDULED" : existing.getStatus();
            if (!status.equalsIgnoreCase(previousStatus)
                    && List.of("POSTPONED", "CANCELLED", "REFUNDED").contains(status.toUpperCase())) {
                notificationDispatchClient.eventChanged(
                        updated,
                        status.toUpperCase(),
                        registrationService.getAccountIdsByEventId(id));
            }
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {

        if (eventService.getEventById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = eventService.getEventById(id).orElseThrow();
        notificationDispatchClient.eventChanged(
                event, "CANCELLED", registrationService.getAccountIdsByEventId(id));
        registrationService.removeForDeletedEvent(id);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}