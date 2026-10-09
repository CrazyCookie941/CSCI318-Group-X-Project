package com.csci318.groupx.registrationservice.presentation.dto;

public class AccountProfileResponse {

    private boolean success;
    private String message;
    private Long accountId;
    private String username;
    private String email;
    private String location;

    public AccountProfileResponse() {
    }

    public AccountProfileResponse(
            boolean success,
            String message,
            Long accountId,
            String username,
            String email,
            String location
    ) {
        this.success = success;
        this.message = message;
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.location = location;
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

    public String getLocation() {
        return location;
    }
}