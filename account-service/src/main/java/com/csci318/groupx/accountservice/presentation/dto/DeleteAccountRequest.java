package com.csci318.groupx.accountservice.presentation.dto;

public class DeleteAccountRequest {

    private String password;

    public DeleteAccountRequest() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
