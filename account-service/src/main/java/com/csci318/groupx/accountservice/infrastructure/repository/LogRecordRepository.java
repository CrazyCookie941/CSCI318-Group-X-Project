package com.csci318.groupx.accountservice.infrastructure.repository;

import com.csci318.groupx.accountservice.domain.model.LogRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogRecordRepository
        extends JpaRepository<LogRecord, Long> {

    List<LogRecord> findAllByOrderByTimestampDesc();

    List<LogRecord> findByAccountIdOrderByTimestampDesc(
            Long accountId
    );
}