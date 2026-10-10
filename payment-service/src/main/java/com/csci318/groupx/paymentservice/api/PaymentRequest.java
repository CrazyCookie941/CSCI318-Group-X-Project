package com.csci318.groupx.paymentservice.api;

/**
 * Coursework-only simulated payment request.
 *
 * No card number, expiry date or CVV is accepted.
 */
public record PaymentRequest(
        Long eventId,
        Long accountId,
        String paymentMethod,
        boolean simulateDecline
) {
}