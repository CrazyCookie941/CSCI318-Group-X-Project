package com.csci318.groupx.paymentservice.service;

import com.csci318.groupx.paymentservice.api.PaymentRequest;
import com.csci318.groupx.paymentservice.api.PaymentResponse;
import com.csci318.groupx.paymentservice.domain.PaymentStatus;
import com.csci318.groupx.paymentservice.repository.PaymentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PaymentService {

    private static final Set<String> ALLOWED_METHODS =
            Set.of("VISA", "MASTERCARD", "PAYPAL");

    private final PaymentRepository repository;
    private final PaymentProcessor paymentProcessor;

    public PaymentService(
            PaymentRepository repository,
            PaymentProcessor paymentProcessor
    ) {
        this.repository = repository;
        this.paymentProcessor = paymentProcessor;
    }

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

        try {
            return paymentProcessor.process(request, method);
        } catch (DataIntegrityViolationException exception) {
            return repository.findFirstByAccountIdAndEventIdAndStatusOrderByIdDesc(
                            request.accountId(), request.eventId(), PaymentStatus.SUCCEEDED)
                    .map(PaymentResponse::from)
                    .orElseThrow(() -> exception);
        }
    }

    public PaymentResponse getById(Long paymentId) {

        return repository.findById(paymentId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found."
                ));
    }

    public List<PaymentResponse> getByAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid accountId is required."
            );
        }
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }
}