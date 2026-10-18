package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import com.csci318.groupx.eventservice.domain.model.EventRegistration;
import com.csci318.groupx.eventservice.infrastructure.repository.EventRegistrationRepository;
import com.csci318.groupx.eventservice.infrastructure.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventRegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;

    public EventRegistrationService(
            EventRegistrationRepository registrationRepository,
            EventRepository eventRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<EventRegistration> getByAccountId(Long accountId) {
        requireValidId(accountId, "accountId");
        return registrationRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public List<Long> getAccountIdsByEventId(Long eventId) {
        requireValidId(eventId, "eventId");
        return registrationRepository.findByEventId(eventId).stream()
                .map(registration -> registration.getAccountId())
                .distinct()
                .toList();
    }

    @Transactional
    public EventRegistration register(Long accountId, Long eventId) {
        requireValidId(accountId, "accountId");
        requireValidId(eventId, "eventId");

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found."
                ));
        var existing = registrationRepository.findByAccountIdAndEventId(accountId, eventId);
        if (existing.isPresent()) {
            return existing.get();
        }

        int registeredCount = event.getRegisteredCount() == null
                ? Math.toIntExact(registrationRepository.countByEventId(eventId))
                : event.getRegisteredCount();
        if (event.getCapacity() != null && registeredCount >= event.getCapacity()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This event has reached capacity."
            );
        }
        if (event.getStatus() != null
                && !event.getStatus().equalsIgnoreCase("SCHEDULED")) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This event is not accepting registrations."
            );
        }

        EventRegistration saved = registrationRepository.save(
                new EventRegistration(accountId, eventId)
        );
        event.setRegisteredCount(registeredCount + 1);
        eventRepository.save(event);
        return saved;
    }

    @Transactional
    public void cancel(Long registrationId, Long accountId) {
        requireValidId(registrationId, "registrationId");
        requireValidId(accountId, "accountId");

        EventRegistration registration = registrationRepository.findById(registrationId)
                .filter(existing -> existing.getAccountId().equals(accountId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Registration not found."
                ));

        registrationRepository.delete(registration);
        eventRepository.findById(registration.getEventId()).ifPresent(event -> {
            if (event.getRegisteredCount() != null && event.getRegisteredCount() > 0) {
                event.setRegisteredCount(event.getRegisteredCount() - 1);
                eventRepository.save(event);
            }
        });
    }

    @Transactional
    public void removeForDeletedEvent(Long eventId) {
        registrationRepository.deleteByEventId(eventId);
    }

    private static void requireValidId(Long id, String field) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid " + field + " is required."
            );
        }
    }
}
