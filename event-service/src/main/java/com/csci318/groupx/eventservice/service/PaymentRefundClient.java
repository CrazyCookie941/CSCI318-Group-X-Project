
package com.csci318.groupx.eventservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class PaymentRefundClient {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentRefundClient.class);

    private final RestClient client;
    private final String token;

    public PaymentRefundClient(
            @Value("${payment.service-url:http://localhost:8085}")
            String baseUrl,
            @Value("${PAYMENT_REFUND_TOKEN:}")
            String token) {

        this.client = RestClient.create(baseUrl);
        this.token = token;
    }

    public void refundEvent(Long eventId) {

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "PAYMENT_REFUND_TOKEN is missing in Event Service."
            );
        }

        try {
            client.post()
                    .uri("/api/payments/events/{id}/refunds", eventId)
                    .header("X-Internal-Token", token)
                    .retrieve()
                    .toBodilessEntity();

            logger.info(
                    "Simulated refund completed for eventId={}",
                    eventId
            );

        } catch (RestClientResponseException ex) {

            int status = ex.getStatusCode().value();

            logger.error(
                    "Payment Service rejected refund for eventId={}, HTTP {}",
                    eventId,
                    status,
                    ex
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Payment Service returned HTTP " + status
                            + " during simulated refund.",
                    ex
            );

        } catch (ResourceAccessException ex) {

            logger.error(
                    "Cannot reach Payment Service for eventId={}",
                    eventId,
                    ex
            );

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cannot connect to Payment Service on port 8085.",
                    ex
            );

        } catch (RestClientException ex) {

            logger.error(
                    "Unexpected refund communication error for eventId={}",
                    eventId,
                    ex
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unexpected refund service error.",
                    ex
            );
        }
    }
}
