package com.csci318.groupx.registrationservice;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

final class PaymentApiClient {

    private static final RestClient CLIENT;

    static {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        CLIENT = RestClient.builder()
                .baseUrl("http://localhost:8084")
                .requestFactory(factory)
                .build();
    }

    record PaymentRequest(
            Long eventId,
            Long accountId,
            String paymentMethod,
            boolean simulateDecline
    ) {
    }

    record PaymentResponse(
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
    }

    private PaymentApiClient() {
    }

    static CompletableFuture<PaymentResponse> simulate(
            long eventId,
            long accountId,
            String method,
            boolean decline
    ) {
        return CompletableFuture.supplyAsync(() ->
                CLIENT.post()
                        .uri("/api/payments/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new PaymentRequest(
                                eventId,
                                accountId,
                                method,
                                decline
                        ))
                        .retrieve()
                        .body(PaymentResponse.class)
        );
    }
}