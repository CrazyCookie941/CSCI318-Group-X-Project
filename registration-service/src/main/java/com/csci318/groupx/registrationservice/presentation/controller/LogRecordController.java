package com.csci318.groupx.registrationservice.presentation.controller;

import com.csci318.groupx.registrationservice.domain.model.LogRecord;
import com.csci318.groupx.registrationservice.service.DatabaseLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogRecordController {

    private final DatabaseLogService databaseLogService;

    public LogRecordController(
            DatabaseLogService databaseLogService
    ) {
        this.databaseLogService = databaseLogService;
    }

    @GetMapping
    public List<LogRecord> getAllLogs() {
        return databaseLogService.getAllLogs();
    }

    @GetMapping("/account/{accountId}")
    public List<LogRecord> getLogsForAccount(
            @PathVariable Long accountId
    ) {
        return databaseLogService.getLogsForAccount(accountId);
    }
}