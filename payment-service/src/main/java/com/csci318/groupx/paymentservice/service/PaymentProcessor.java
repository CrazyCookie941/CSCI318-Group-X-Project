package com.csci318.groupx.paymentservice.service;

import com.csci318.groupx.paymentservice.api.PaymentRequest;
import com.csci318.groupx.paymentservice.api.PaymentResponse;
import com.csci318.groupx.paymentservice.domain.PaymentRecord;
import com.csci318.groupx.paymentservice.domain.PaymentStatus;
import com.csci318.groupx.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
        var previousPayment =
                repository.findFirstByAccountIdAndEventIdAndStatusOrderByIdDesc(
                        request.accountId(), request.eventId(), PaymentStatus.SUCCEEDED);
        if (previousPayment.isPresent()) {
            return PaymentResponse.from(previousPayment.get());
        }

        EventCatalogClient.EventSnapshot event = eventCatalog.requireEvent(request.eventId());
        if (event.price() == null
                || event.price().signum() < 0
                || event.price().compareTo(new BigDecimal("999999.99")) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Event has an invalid ticket price."
            );
        }

        BigDecimal amount = event.price().setScale(2, RoundingMode.HALF_UP);
        PaymentStatus status = request.simulateDecline()
                ? PaymentStatus.DECLINED
                : PaymentStatus.SUCCEEDED;
        String eventName = event.name() == null || event.name().isBlank()
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
        if (status == PaymentStatus.SUCCEEDED) {
            record.setSuccessfulPaymentKey(request.accountId() + ":" + request.eventId());
        }

        return PaymentResponse.from(repository.saveAndFlush(record));
    }
}
