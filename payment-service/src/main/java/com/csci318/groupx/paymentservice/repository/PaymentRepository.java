package com.csci318.groupx.paymentservice.repository;

import com.csci318.groupx.paymentservice.domain.PaymentRecord;
import com.csci318.groupx.paymentservice.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findFirstByAccountIdAndEventIdAndStatusOrderByIdDesc(
            Long accountId, Long eventId, PaymentStatus status);

    List<PaymentRecord> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentRecord p WHERE p.eventId = :eventId AND p.status = :status")
    List<PaymentRecord> findByEventIdAndStatusForUpdate(
            @Param("eventId") Long eventId,
            @Param("status") PaymentStatus status);
}
