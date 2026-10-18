
package com.csci318.groupx.eventservice.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class ChatTelemetryFilter extends OncePerRequestFilter {

    private final ChatTelemetry telemetry;

    public ChatTelemetryFilter(ChatTelemetry telemetry) {
        this.telemetry = telemetry;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        // Only monitor the Chatbot POST endpoint.
        return !(
                "POST".equalsIgnoreCase(request.getMethod())
                        && "/api/chat".equals(request.getServletPath())
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.nanoTime();
        boolean completedNormally = false;

        try {

            filterChain.doFilter(request, response);
            completedNormally = true;

        } finally {

            long durationMs = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startTime
            );

            int status = response.getStatus();

            if (!completedNormally && status < 400) {
                status = 500;
            }

            telemetry.record(
                    durationMs,
                    status,
                    completedNormally
            );
        }
    }
}
