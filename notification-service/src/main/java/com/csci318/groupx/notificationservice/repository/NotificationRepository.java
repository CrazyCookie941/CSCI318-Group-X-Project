package com.csci318.groupx.notificationservice.repository;

import com.csci318.groupx.notificationservice.domain.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationRecord, Long> {

    List<NotificationRecord> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    long countByAccountIdAndReadFalse(Long accountId);

    Optional<NotificationRecord> findByIdAndAccountId(Long id, Long accountId);

    long deleteByIdAndAccountId(Long id, Long accountId);
}
