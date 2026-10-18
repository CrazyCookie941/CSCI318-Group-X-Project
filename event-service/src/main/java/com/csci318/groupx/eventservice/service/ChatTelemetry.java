
package com.csci318.groupx.eventservice.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

@Service
public class ChatTelemetry {

    // HTTP request telemetry
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder successfulRequests = new LongAdder();
    private final LongAdder failedRequests = new LongAdder();
    private final LongAdder totalResponseTimeMs = new LongAdder();

    private final AtomicLong lastResponseTimeMs =
            new AtomicLong(0);

    private final AtomicInteger lastHttpStatus =
            new AtomicInteger(0);

    // Agentic AI tool telemetry
    private final LongAdder totalToolCalls = new LongAdder();
    private final LongAdder successfulToolCalls = new LongAdder();
    private final LongAdder failedToolCalls = new LongAdder();
    private final LongAdder totalToolTimeMs = new LongAdder();

    private final AtomicLong lastToolTimeMs =
            new AtomicLong(0);

    private final AtomicReference<String> lastToolName =
            new AtomicReference<>("none");

    public void record(
            long responseTimeMs,
            int httpStatus,
            boolean completedNormally
    ) {

        totalRequests.increment();

        if (completedNormally &&
                httpStatus >= 200 &&
                httpStatus < 300) {

            successfulRequests.increment();

        } else {

            failedRequests.increment();
        }

        totalResponseTimeMs.add(responseTimeMs);
        lastResponseTimeMs.set(responseTimeMs);
        lastHttpStatus.set(httpStatus);
    }

    public void recordToolCall(
            String toolName,
            long executionTimeMs,
            boolean success
    ) {

        totalToolCalls.increment();

        if (success) {
            successfulToolCalls.increment();
        } else {
            failedToolCalls.increment();
        }

        totalToolTimeMs.add(executionTimeMs);
        lastToolTimeMs.set(executionTimeMs);
        lastToolName.set(toolName);
    }

    public Map<String, Object> getSnapshot() {

        long total = totalRequests.sum();
        long success = successfulRequests.sum();
        long failure = failedRequests.sum();

        double successRate = percentage(success, total);

        double averageTime = average(
                totalResponseTimeMs.sum(),
                total
        );

        long toolTotal = totalToolCalls.sum();
        long toolSuccess = successfulToolCalls.sum();
        long toolFailure = failedToolCalls.sum();

        Map<String, Object> result = new LinkedHashMap<>();

        // Existing HTTP monitoring fields
        result.put("totalRequests", total);
        result.put("successfulRequests", success);
        result.put("failedRequests", failure);
        result.put("successRatePercent", successRate);
        result.put("averageResponseTimeMs", averageTime);
        result.put("lastResponseTimeMs", lastResponseTimeMs.get());
        result.put("lastHttpStatus", lastHttpStatus.get());

        // New Agentic AI monitoring fields
        result.put("totalToolCalls", toolTotal);
        result.put("successfulToolCalls", toolSuccess);
        result.put("failedToolCalls", toolFailure);

        result.put(
                "toolSuccessRatePercent",
                percentage(toolSuccess, toolTotal)
        );

        result.put(
                "averageToolExecutionTimeMs",
                average(totalToolTimeMs.sum(), toolTotal)
        );

        result.put("lastToolName", lastToolName.get());
        result.put("lastToolExecutionTimeMs", lastToolTimeMs.get());

        return result;
    }

    private double percentage(long part, long total) {

        if (total == 0) {
            return 0.0;
        }

        return Math.round(part * 10000.0 / total) / 100.0;
    }

    private double average(long sum, long count) {

        if (count == 0) {
            return 0.0;
        }

        return Math.round(sum * 100.0 / count) / 100.0;
    }
}
