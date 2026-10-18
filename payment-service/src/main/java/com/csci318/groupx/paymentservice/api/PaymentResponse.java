package com.csci318.groupx.paymentservice.api;

import com.csci318.groupx.paymentservice.domain.PaymentRecord;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long paymentId,
        String reference,
        Long eventId,
        Long accountId,
        String eventName,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String status,
        Instant createdAt,
        String message
) {
    public static PaymentResponse from(PaymentRecord record) {
        String message = switch (record.getStatus()) {
            case SUCCEEDED -> "Simulated payment successful. No real charge occurred.";
            case DECLINED -> "Simulated payment declined. No real charge occurred.";
            case REFUNDED -> "Simulated refund completed. No real money was transferred.";
        };
        return new PaymentResponse(
                record.getId(), record.getReference(), record.getEventId(),
                record.getAccountId(), record.getEventName(), record.getAmount(),
                record.getCurrency(), record.getPaymentMethod(),
                record.getStatus().name(), record.getCreatedAt(), message);
    }
}
