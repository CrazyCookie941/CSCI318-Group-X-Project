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
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Set<String> ALLOWED_METHODS =
            Set.of("VISA", "MASTERCARD", "PAYPAL");

    private final PaymentRepository repository;
    private final EventCatalogClient eventCatalog;

    public PaymentService(
            PaymentRepository repository,
            EventCatalogClient eventCatalog
    ) {
        this.repository = repository;
        this.eventCatalog = eventCatalog;
    }

    @Transactional
    public PaymentResponse simulate(PaymentRequest request) {

        if (request == null
                || request.accountId() == null
                || request.accountId() <= 0
                || request.eventId() == null
                || request.eventId() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid accountId and eventId are required."
            );
        }

        String method = request.paymentMethod() == null
                ? ""
                : request.paymentMethod()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!ALLOWED_METHODS.contains(method)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Select Visa, Mastercard, or PayPal."
            );
        }

        // Reuse an existing successful payment.
        var previousPayment =
                repository.findFirstByAccountIdAndEventIdAndStatusOrderByIdDesc(
                        request.accountId(),
                        request.eventId(),
                        PaymentStatus.SUCCEEDED
                );

        if (previousPayment.isPresent()) {
            return PaymentResponse.from(previousPayment.get());
        }

        // Get the ticket price from Event Service.
        EventCatalogClient.EventSnapshot event =
                eventCatalog.requireEvent(request.eventId());

        if (event.price() == null
                || event.price().signum() < 0
                || event.price().compareTo(
                new BigDecimal("999999.99")) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event has an invalid ticket price."
            );
        }

        BigDecimal amount = event.price()
                .setScale(2, RoundingMode.HALF_UP);

        PaymentStatus status = request.simulateDecline()
                ? PaymentStatus.DECLINED
                : PaymentStatus.SUCCEEDED;

        String eventName =
                event.name() == null || event.name().isBlank()
                        ? "Event #" + request.eventId()
                        : event.name();

        PaymentRecord record = new PaymentRecord(
                request.eventId(),
                request.accountId(),
                eventName,
                amount,
                "AUD",
                method,
                status,
                UUID.randomUUID().toString(),
                Instant.now()
        );

        PaymentRecord saved = repository.save(record);

        return PaymentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(Long paymentId) {

        return repository.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found."
                ));
    }
}