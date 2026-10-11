package com.csci318.groupx.paymentservice.repository;

import com.csci318.groupx.paymentservice.domain.PaymentRecord;
import com.csci318.groupx.paymentservice.domain.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord>
    findFirstByAccountIdAndEventIdAndStatusOrderByIdDesc(
            Long accountId,
            Long eventId,
            PaymentStatus status
    );
}