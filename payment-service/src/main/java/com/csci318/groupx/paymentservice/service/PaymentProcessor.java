package com.csci318.groupx.paymentservice.service;

import com.csci318.groupx.paymentservice.api.PaymentRequest;
import com.csci318.groupx.paymentservice.api.PaymentResponse;
import com.csci318.groupx.paymentservice.domain.PaymentRecord;
import com.csci318.groupx.paymentservice.domain.PaymentStatus;
import com.csci318.groupx.paymentservice.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentProcessor {
    private final PaymentRepository repository;
    private final EventCatalogClient eventCatalog;

    public PaymentProcessor(PaymentRepository repository, EventCatalogClient eventCatalog) {
        this.repository = repository;
        this.eventCatalog = eventCatalog;
    }

    @Transactional
    public PaymentResponse process(PaymentRequest request, String method) {
        // Validate the current event before returning any old successful payment.
        EventCatalogClient.EventSnapshot event = eventCatalog.requireEvent(request.eventId());
        if (!"SCHEDULED".equalsIgnoreCase(event.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This event is not accepting payments.");
        }

        var previous = repository.findFirstByAccountIdAndEventIdAndStatusOrderByIdDesc(
                request.accountId(), request.eventId(), PaymentStatus.SUCCEEDED);
        if (previous.isPresent()) {
            return PaymentResponse.from(previous.get());
        }

        // Capacity blocks NEW registrations, not payments by people already registered.
        // For an event that is full, verify the account has an existing registration.
        if (event.capacity() != null && event.registeredCount() != null
                && event.registeredCount() >= event.capacity()
                && !eventCatalog.isRegisteredForEvent(request.accountId(), request.eventId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This event has reached capacity and this account is not registered.");
        }
        if (event.price() == null || event.price().signum() < 0
                || event.price().compareTo(new BigDecimal("999999.99")) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event has an invalid ticket price.");
        }
        BigDecimal amount = event.price().setScale(2, RoundingMode.HALF_UP);
        PaymentStatus status = request.simulateDecline()
                ? PaymentStatus.DECLINED : PaymentStatus.SUCCEEDED;
        PaymentRecord record = new PaymentRecord(
                request.eventId(), request.accountId(), event.name(), amount,
                "AUD", method, status, UUID.randomUUID().toString(), Instant.now());
        if (status == PaymentStatus.SUCCEEDED) {
            record.setSuccessfulPaymentKey(request.accountId() + ":" + request.eventId());
        }
        return PaymentResponse.from(repository.saveAndFlush(record));
    }
}
