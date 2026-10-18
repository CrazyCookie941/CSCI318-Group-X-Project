package com.csci318.groupx.registrationservice;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;

final class AccountApiClient {
    private static final RestClient CLIENT = RestClient.create("http://localhost:8083");

    private AccountApiClient() {
    }

    static CompletableFuture<Profile> profile(long accountId) {
        return CompletableFuture.supplyAsync(() -> CLIENT.get()
                .uri("/api/accounts/{accountId}", accountId)
                .retrieve()
                .body(Profile.class));
    }

    static CompletableFuture<Profile> update(
            long accountId, String username, String email, String location
    ) {
        return CompletableFuture.supplyAsync(() -> CLIENT.put()
                .uri("/api/accounts/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateRequest(username, email, location))
                .retrieve()
                .body(Profile.class));
    }

    record Profile(
            boolean success,
            String message,
            Long accountId,
            String username,
            String email,
            String location
    ) {
    }

    private record UpdateRequest(String username, String email, String location) {
    }
}
