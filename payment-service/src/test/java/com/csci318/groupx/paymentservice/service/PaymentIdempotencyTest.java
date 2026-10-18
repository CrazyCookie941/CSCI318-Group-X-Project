package com.csci318.groupx.paymentservice.service;

import com.csci318.groupx.paymentservice.api.PaymentRequest;
import com.csci318.groupx.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:payment_idempotency_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
class PaymentIdempotencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private EventCatalogClient eventCatalog;

    @Test
    void concurrentSuccessfulRetriesCreateOnlyOnePayment() throws Exception {
        when(eventCatalog.requireEvent(55L)).thenReturn(
                new EventCatalogClient.EventSnapshot(55L, "Concurrency test", new BigDecimal("25.00")));

        int requestCount = 20;
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(requestCount)) {
            var attempts = java.util.stream.IntStream.range(0, requestCount)
                    .mapToObj(index -> executor.submit(() -> {
                        ready.countDown();
                        start.await();
                        return paymentService.simulate(
                                new PaymentRequest(55L, 700L, "VISA", false));
                    }))
                    .toList();

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            Set<Long> paymentIds = new java.util.HashSet<>();
            for (var attempt : attempts) {
                paymentIds.add(attempt.get(20, TimeUnit.SECONDS).paymentId());
            }

            assertEquals(1, paymentIds.size());
            assertEquals(1, paymentRepository.findByAccountIdOrderByCreatedAtDesc(700L).size());
        }
    }
}
