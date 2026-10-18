package com.csci318.groupx.registrationservice;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;

final class NotificationApiClient {

    private static final RestClient CLIENT = RestClient.create("http://localhost:8084");

    private NotificationApiClient() {
    }

    static List<Notification> list(long accountId) {
        List<Notification> notifications = CLIENT.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notifications")
                        .queryParam("accountId", accountId)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Notification>>() {
                });
        return notifications == null ? List.of() : notifications;
    }

    static long unreadCount(long accountId) {
        UnreadCount response = CLIENT.get()
                .uri(uriBuilder -> uriBuilder.path("/api/notifications/unread-count")
                        .queryParam("accountId", accountId)
                        .build())
                .retrieve()
                .body(UnreadCount.class);
        return response == null ? 0 : response.count();
    }

    static void markRead(long notificationId, long accountId) {
        CLIENT.put()
                .uri(uriBuilder -> uriBuilder.path("/api/notifications/{id}/read")
                        .queryParam("accountId", accountId)
                        .build(notificationId))
                .retrieve()
                .toBodilessEntity();
    }

    static void clear(long notificationId, long accountId) {
        CLIENT.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/notifications/{id}")
                        .queryParam("accountId", accountId)
                        .build(notificationId))
                .retrieve()
                .toBodilessEntity();
    }

    static Preference preference(long accountId) {
        return CLIENT.get()
                .uri("/api/notifications/preferences/{accountId}", accountId)
                .retrieve()
                .body(Preference.class);
    }

    static Preference savePreference(long accountId, boolean enabled, String area) {
        return CLIENT.put()
                .uri("/api/notifications/preferences/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Preference(enabled, area))
                .retrieve()
                .body(Preference.class);
    }

    record Notification(
            Long id,
            Long accountId,
            Long eventId,
            String title,
            String message,
            String createdAt,
            boolean read
    ) {
    }

    record UnreadCount(long count) {
    }

    record Preference(boolean enabled, String area) {
    }
}
