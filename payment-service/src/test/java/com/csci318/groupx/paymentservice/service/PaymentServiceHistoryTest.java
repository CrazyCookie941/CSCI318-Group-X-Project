
package com.csci318.groupx.paymentservice.service;

import com.csci318.groupx.paymentservice.domain.PaymentRecord;
import com.csci318.groupx.paymentservice.domain.PaymentStatus;
import com.csci318.groupx.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceHistoryTest {

    private static final String TEST_REFUND_TOKEN = "unit-test-refund-token";

    @Test
    void paymentHistoryReturnsOnlyAccountRecordsInRepositoryOrder() {

        PaymentRepository repository = mock(PaymentRepository.class);

        PaymentRecord payment = new PaymentRecord(
                5L,
                12L,
                "Community concert",
                new BigDecimal("24.50"),
                "AUD",
                "VISA",
                PaymentStatus.SUCCEEDED,
                "reference",
                Instant.parse("2026-10-17T00:00:00Z")
        );

        when(repository.findByAccountIdOrderByCreatedAtDesc(12L))
                .thenReturn(List.of(payment));

        PaymentService service = new PaymentService(
                repository,
                mock(PaymentProcessor.class),
                TEST_REFUND_TOKEN
        );

        var history = service.getByAccountId(12L);

        assertEquals(1, history.size());
        assertEquals("Community concert", history.getFirst().eventName());
        assertEquals(new BigDecimal("24.50"), history.getFirst().amount());
        assertEquals("SUCCEEDED", history.getFirst().status());

        verify(repository).findByAccountIdOrderByCreatedAtDesc(12L);
    }

    @Test
    void paymentHistoryRejectsInvalidAccountIds() {

        PaymentService service = new PaymentService(
                mock(PaymentRepository.class),
                mock(PaymentProcessor.class),
                TEST_REFUND_TOKEN
        );

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.getByAccountId(0L)
        );
    }
}
