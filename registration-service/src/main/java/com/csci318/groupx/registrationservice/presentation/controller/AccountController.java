package com.csci318.groupx.registrationservice.presentation.controller;

import com.csci318.groupx.registrationservice.presentation.dto.AccountProfileResponse;
import com.csci318.groupx.registrationservice.presentation.dto.UpdateAccountRequest;
import com.csci318.groupx.registrationservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(
            AccountService accountService
    ) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountProfileResponse> getAccountProfile(
            @PathVariable Long accountId
    ) {

        AccountProfileResponse response =
                accountService.getAccountProfile(accountId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountProfileResponse> updateAccountProfile(
            @PathVariable Long accountId,
            @RequestBody UpdateAccountRequest request
    ) {

        AccountProfileResponse response =
                accountService.updateAccountProfile(
                        accountId,
                        request
                );

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        if ("Account not found.".equals(
                response.getMessage())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<AccountProfileResponse> deleteAccount(
            @PathVariable Long accountId
    ) {

        AccountProfileResponse response =
                accountService.deleteAccount(accountId);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        if ("Account not found.".equals(
                response.getMessage())) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}