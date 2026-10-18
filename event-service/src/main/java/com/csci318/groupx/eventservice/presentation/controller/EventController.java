package com.csci318.groupx.eventservice.presentation.controller;

import com.csci318.groupx.eventservice.domain.model.Event;
import com.csci318.groupx.eventservice.presentation.dto.EventScoreResponse;
import com.csci318.groupx.eventservice.service.EventRegistrationService;
import com.csci318.groupx.eventservice.service.EventService;
import com.csci318.groupx.eventservice.service.NotificationDispatchClient;
import com.csci318.groupx.eventservice.service.PaymentRefundClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final EventRegistrationService registrationService;
    private final NotificationDispatchClient notificationDispatchClient;
    private final PaymentRefundClient paymentRefundClient;

    public EventController(EventService eventService,
            EventRegistrationService registrationService,
            NotificationDispatchClient notificationDispatchClient,
            PaymentRefundClient paymentRefundClient) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.notificationDispatchClient = notificationDispatchClient;
        this.paymentRefundClient = paymentRefundClient;
    }

    @GetMapping
    public List<Event> getAllEvents() { return eventService.getAllEvents(); }

    @GetMapping("/filter")
    public List<Event> filterEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double maxPrice) {
        return eventService.filterEvents(category, location, maxPrice);
    }

    @GetMapping("/search")
    public List<Event> searchEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double maxPrice) {
        return eventService.searchEvents(q, category, location, maxPrice);
    }

    @GetMapping("/rank")
    public List<EventScoreResponse> rankEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double maxPrice) {
        return eventService.rankEvents(q, category, location, maxPrice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        Event created = eventService.createEvent(event);
        notificationDispatchClient.eventCreated(created);
        return created;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        Event existing = eventService.getEventById(id).orElse(null);
        if (existing == null) { return ResponseEntity.notFound().build(); }
        String previousStatus = existing.getStatus(); // Capture BEFORE mutating the entity.
        Event updated = eventService.updateEvent(id, event);
        String currentStatus = updated.getStatus();

        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            // Always retry an incomplete cancellation, even if status was already CANCELLED.
            completeCancellation(updated);
        } else if (!currentStatus.equalsIgnoreCase(previousStatus)
                && List.of("POSTPONED", "REFUNDED").contains(currentStatus.toUpperCase())) {
            notificationDispatchClient.eventChanged(updated, currentStatus.toUpperCase(),
                    registrationService.getAccountIdsByEventId(id));
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        if (eventService.getEventById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Cancel first so new registrations and payments are blocked.
        Event cancelled = eventService.markCancelled(id);
        completeCancellation(cancelled);
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    private void completeCancellation(Event event) {
        Long eventId = event.getId();
        List<Long> registeredAccounts = registrationService.getAccountIdsByEventId(eventId);
        // Fail before deleting registrations if the remote refund cannot be completed.
        paymentRefundClient.refundEvent(eventId);
        // Also fail before cleanup when required notifications cannot be saved.
        notificationDispatchClient.eventCancelledRequired(event, registeredAccounts);
        registrationService.removeForDeletedEvent(eventId);
        eventService.clearRegisteredCount(eventId);
    }
}
