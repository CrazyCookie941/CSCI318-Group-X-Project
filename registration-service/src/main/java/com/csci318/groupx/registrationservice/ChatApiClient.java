package com.csci318.groupx.registrationservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

final class ChatApiClient {

    private static final RestClient CLIENT = RestClient.create("http://localhost:8081");

    private ChatApiClient() {
    }

    static ChatResponse send(String message) {
        return CLIENT.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChatRequest(message))
                .retrieve()
                .body(ChatResponse.class);
    }

    static ChatStatus getStatus() {
        return CLIENT.get()
                .uri("/api/chat/status")
                .retrieve()
                .body(ChatStatus.class);
    }

    private record ChatRequest(String message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatResponse(String reply, String model, int eventsConsidered) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatStatus(boolean configured, String model) {
    }
}
