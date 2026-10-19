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

    static CompletableFuture<byte[]> profilePicture(long accountId) {
        return CompletableFuture.supplyAsync(() -> CLIENT.get()
                .uri("/api/accounts/{accountId}/profile-picture", accountId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (request, response) -> {})
                .body(byte[].class));
    }

    static CompletableFuture<Void> updateProfilePicture(
            long accountId, byte[] picture, String contentType
    ) {
        return CompletableFuture.runAsync(() -> CLIENT.put()
                .uri("/api/accounts/{accountId}/profile-picture", accountId)
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .body(picture)
                .retrieve()
                .toBodilessEntity());
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
