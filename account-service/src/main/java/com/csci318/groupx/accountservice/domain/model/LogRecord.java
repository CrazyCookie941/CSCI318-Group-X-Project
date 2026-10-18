package com.csci318.groupx.accountservice.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_records")
public class LogRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String logLevel;

    @Column(nullable = false)
    private String action;

    private Long accountId;

    @Column(length = 1000)
    private String message;

    public LogRecord() {
    }

    public LogRecord(
            String logLevel,
            String action,
            Long accountId,
            String message
    ) {
        this.timestamp = LocalDateTime.now();
        this.logLevel = logLevel;
        this.action = action;
        this.accountId = accountId;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public String getAction() {
        return action;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getMessage() {
        return message;
    }
}