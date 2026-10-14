
package com.csci318.groupx.eventservice.presentation.controller;

import com.csci318.groupx.eventservice.service.ChatTelemetry;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat/telemetry")
public class ChatTelemetryController {

    private final ChatTelemetry telemetry;

    public ChatTelemetryController(ChatTelemetry telemetry) {
        this.telemetry = telemetry;
    }

    @GetMapping
    public Map<String, Object> getTelemetry() {
        return telemetry.getSnapshot();
    }
}
