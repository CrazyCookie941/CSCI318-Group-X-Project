package com.csci318.groupx.eventservice.presentation.controller;

import com.csci318.groupx.eventservice.domain.model.EventRegistration;
import com.csci318.groupx.eventservice.presentation.dto.EventRegistrationRequest;
import com.csci318.groupx.eventservice.service.EventRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class EventRegistrationController {

    private final EventRegistrationService registrationService;

    public EventRegistrationController(EventRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public List<EventRegistration> getRegistrations(
            @RequestParam Long accountId
    ) {
        return registrationService.getByAccountId(accountId);
    }

    @PostMapping
    public ResponseEntity<EventRegistration> register(
            @RequestBody EventRegistrationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                registrationService.register(
                        request == null ? null : request.accountId(),
                        request == null ? null : request.eventId()
                )
        );
    }

    @DeleteMapping("/{registrationId}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long registrationId,
            @RequestParam Long accountId
    ) {
        registrationService.cancel(registrationId, accountId);
        return ResponseEntity.noContent().build();
    }
}
