package com.csci318.groupx.eventservice.presentation.controller;

import com.csci318.groupx.eventservice.presentation.dto.ChatRequest;
import com.csci318.groupx.eventservice.presentation.dto.ChatResponse;
import com.csci318.groupx.eventservice.service.ChatbotService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatbotService chatbotService;

    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Main endpoint for JavaFX chatbot integration.
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ChatResponse chat(
            @RequestBody ChatRequest request
    ) {
        return chatbotService.chat(request);
    }

    /**
     * Check chatbot configuration without exposing
     * the API key.
     */
    @GetMapping("/status")
    public Map<String, Object> status() {

        return Map.of(
                "configured", chatbotService.isConfigured(),
                "model", chatbotService.getModelName()
        );
    }
}