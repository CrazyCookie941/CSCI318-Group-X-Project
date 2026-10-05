package com.csci318.groupx.registrationservice.service;

import com.csci318.groupx.registrationservice.domain.model.LogRecord;
import com.csci318.groupx.registrationservice.infrastructure.repository.LogRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseLogService {

    private static final Logger logger =
            LoggerFactory.getLogger(DatabaseLogService.class);

    private final LogRecordRepository logRecordRepository;

    public DatabaseLogService(
            LogRecordRepository logRecordRepository
    ) {
        this.logRecordRepository = logRecordRepository;
    }

    public void info(
            String action,
            Long accountId,
            String message
    ) {
        save(
                "INFO",
                action,
                accountId,
                message
        );
    }

    public void warn(
            String action,
            Long accountId,
            String message
    ) {
        save(
                "WARN",
                action,
                accountId,
                message
        );
    }

    public void error(
            String action,
            Long accountId,
            String message
    ) {
        save(
                "ERROR",
                action,
                accountId,
                message
        );
    }

    public List<LogRecord> getAllLogs() {
        return logRecordRepository
                .findAllByOrderByTimestampDesc();
    }

    public List<LogRecord> getLogsForAccount(
            Long accountId
    ) {
        return logRecordRepository
                .findByAccountIdOrderByTimestampDesc(
                        accountId
                );
    }

    private void save(
            String level,
            String action,
            Long accountId,
            String message
    ) {
        try {
            LogRecord logRecord =
                    new LogRecord(
                            level,
                            action,
                            accountId,
                            message
                    );

            logRecordRepository.save(logRecord);

        } catch (Exception e) {

            /*
             * A logging failure should not break
             * the user's main business operation.
             */
            logger.error(
                    "Failed to save database log. action={}, accountId={}",
                    action,
                    accountId,
                    e
            );
        }
    }
}