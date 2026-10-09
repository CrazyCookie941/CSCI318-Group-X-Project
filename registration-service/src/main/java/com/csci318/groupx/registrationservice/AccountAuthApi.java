package com.csci318.groupx.registrationservice;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Client for the existing Spring Boot account authentication endpoints. */
final class AccountAuthApi {

    private static final String BASE_URL = "http://localhost:8083/api/auth";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    record Result(
            boolean success,
            String message,
            Long accountId,
            String username,
            String email,
            String role
    ) {}

    private AccountAuthApi() {}

    static CompletableFuture<Result> login(String identifier, String password) {
        String body = "{\"identifier\":\"" + escape(identifier)
                + "\",\"password\":\"" + escape(password) + "\"}";
        return post("/login", body);
    }

    static CompletableFuture<Result> signup(
            String username, String email, String password, String role
    ) {
        String body = "{\"username\":\"" + escape(username)
                + "\",\"email\":\"" + escape(email)
                + "\",\"password\":\"" + escape(password)
                + "\",\"role\":\"" + escape(role) + "\"}";
        return post("/signup", body);
    }

    private static CompletableFuture<Result> post(String endpoint, String body) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        return CLIENT.sendAsync(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        ).thenApply(response -> {
            String json = response.body();
            String message = stringField(json, "message");
            boolean success = response.statusCode() == 200
                    && booleanField(json, "success");

            if (message == null || message.isBlank()) {
                message = "Request failed (HTTP " + response.statusCode() + ").";
            }

            return new Result(
                    success,
                    message,
                    longField(json, "accountId"),
                    stringField(json, "username"),
                    stringField(json, "email"),
                    stringField(json, "role")
            );
        });
    }

    // Responses from these endpoints are flat JSON objects.
    private static String stringField(String json, String key) {
        Pattern pattern = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        String raw = matcher.group(1);
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch != '\\' || i + 1 >= raw.length()) {
                decoded.append(ch);
                continue;
            }
            char next = raw.charAt(++i);
            switch (next) {
                case 'n' -> decoded.append('\n');
                case 'r' -> decoded.append('\r');
                case 't' -> decoded.append('\t');
                case 'b' -> decoded.append('\b');
                case 'f' -> decoded.append('\f');
                case 'u' -> {
                    if (i + 4 < raw.length()) {
                        try {
                            decoded.append((char) Integer.parseInt(
                                    raw.substring(i + 1, i + 5), 16));
                            i += 4;
                        } catch (NumberFormatException e) {
                            decoded.append('u');
                        }
                    } else {
                        decoded.append('u');
                    }
                }
                default -> decoded.append(next);
            }
        }
        return decoded.toString();
    }

    private static Long longField(String json, String key) {
        Matcher matcher = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)"
        ).matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    private static boolean booleanField(String json, String key) {
        Matcher matcher = Pattern.compile(
                "\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)"
        ).matcher(json);
        return matcher.find() && Boolean.parseBoolean(matcher.group(1));
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
