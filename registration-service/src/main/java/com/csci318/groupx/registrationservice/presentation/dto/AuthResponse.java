package com.csci318.groupx.registrationservice.presentation.dto;

public class AuthResponse {

    private boolean success;
    private String message;
    private Long accountId;
    private String username;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(
            boolean success,
            String message,
            Long accountId,
            String username,
            String email
    ) {
        this.success = success;
        this.message = message;
        this.accountId = accountId;
        this.username = username;
        this.email = email;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}