package com.csci318.groupx.paymentservice.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "simulated_payments")
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 255)
    private String eventName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 20)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(
            name = "payment_reference",
            nullable = false,
            unique = true,
            length = 36
    )
    private String reference;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(name = "successful_payment_key", unique = true, length = 64)
    private String successfulPaymentKey;

    protected PaymentRecord() {
    }

    public PaymentRecord(
            Long eventId,
            Long accountId,
            String eventName,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            PaymentStatus status,
            String reference,
            Instant createdAt
    ) {
        this.eventId = eventId;
        this.accountId = accountId;
        this.eventName = eventName;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.reference = reference;
        this.createdAt = createdAt;
    }

    /** Mark a simulated successful payment as refunded without deleting its audit record. */
    public void markRefunded() {
        if (status != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Only successful payments can be refunded.");
        }
        status = PaymentStatus.REFUNDED;
        successfulPaymentKey = null; // Release the unique key for this account/event.
    }

    public void setSuccessfulPaymentKey(String successfulPaymentKey) {
        this.successfulPaymentKey = successfulPaymentKey;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getEventName() {
        return eventName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getReference() {
        return reference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
