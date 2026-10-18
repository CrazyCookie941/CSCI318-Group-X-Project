package com.csci318.groupx.paymentservice.service;

import com.csci318.groupx.paymentservice.api.PaymentRequest;
import com.csci318.groupx.paymentservice.api.PaymentResponse;
import com.csci318.groupx.paymentservice.domain.PaymentStatus;
import com.csci318.groupx.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PaymentService {
    private static final Set<String> ALLOWED_METHODS =
            Set.of("VISA", "MASTERCARD", "PAYPAL");

    private final PaymentRepository repository;
    private final PaymentProcessor paymentProcessor;
    private final String internalRefundToken;

    public PaymentService(
            PaymentRepository repository,
            PaymentProcessor paymentProcessor,
            @Value("${PAYMENT_REFUND_TOKEN:}") String internalRefundToken) {
        this.repository = repository;
        this.paymentProcessor = paymentProcessor;
        this.internalRefundToken = internalRefundToken;
    }

    public PaymentResponse simulate(PaymentRequest request) {
        if (request == null || request.accountId() == null || request.accountId() <= 0
                || request.eventId() == null || request.eventId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Valid accountId and eventId are required.");
        }
        String method = request.paymentMethod() == null ? ""
                : request.paymentMethod().trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_METHODS.contains(method)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Select Visa, Mastercard, or PayPal.");
        }
        try {
            return paymentProcessor.process(request, method);
        } catch (DataIntegrityViolationException ex) {
            // A concurrent request may have completed the same simulated payment.
            return repository.findFirstByAccountIdAndEventIdAndStatusOrderByIdDesc(
                    request.accountId(), request.eventId(), PaymentStatus.SUCCEEDED)
                    .map(PaymentResponse::from).orElseThrow(() -> ex);
        }
    }

    public PaymentResponse getById(Long paymentId) {
        return repository.findById(paymentId).map(PaymentResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found."));
    }

    public List<PaymentResponse> getByAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A valid accountId is required.");
        }
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(PaymentResponse::from).toList();
    }

    public record RefundSummary(Long eventId, int refundedCount,
                                BigDecimal totalRefunded, String currency) {}

    @Transactional
    public RefundSummary refundEvent(Long eventId, String providedToken) {
        if (internalRefundToken == null || internalRefundToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Internal refund token not configured.");
        }
        if (providedToken == null || !MessageDigest.isEqual(
                internalRefundToken.getBytes(StandardCharsets.UTF_8),
                providedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Internal refund request not authorised.");
        }
        if (eventId == null || eventId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid eventId required.");
        }

        var payments = repository.findByEventIdAndStatusForUpdate(
                eventId, PaymentStatus.SUCCEEDED);
        BigDecimal total = BigDecimal.ZERO;
        for (var payment : payments) {
            total = total.add(payment.getAmount());
            payment.markRefunded();
        }
        repository.saveAllAndFlush(payments);
        return new RefundSummary(eventId, payments.size(), total, "AUD");
    }
}
