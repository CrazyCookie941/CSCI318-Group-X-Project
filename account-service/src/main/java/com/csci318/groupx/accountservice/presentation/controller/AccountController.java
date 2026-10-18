package com.csci318.groupx.accountservice.presentation.controller;

import com.csci318.groupx.accountservice.presentation.dto.AccountProfileResponse;
import com.csci318.groupx.accountservice.presentation.dto.ChangePasswordRequest;
import com.csci318.groupx.accountservice.presentation.dto.DeleteAccountRequest;
import com.csci318.groupx.accountservice.presentation.dto.PasswordChangeResponse;
import com.csci318.groupx.accountservice.presentation.dto.UpdateAccountRequest;
import com.csci318.groupx.accountservice.service.AccountDeletionResult;
import com.csci318.groupx.accountservice.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
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
                accountService.updateAccountProfile(accountId, request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        if ("Account not found.".equals(response.getMessage())) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @PutMapping("/{accountId}/password")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @PathVariable Long accountId,
            @RequestBody ChangePasswordRequest request
    ) {
        PasswordChangeResponse response =
                accountService.changePassword(accountId, request);

        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        if ("Account not found.".equals(response.message())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if ("Current password is incorrect.".equals(response.message())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<AccountDeletionResult> deleteAccount(
            @PathVariable Long accountId,
            @RequestBody(required = false) DeleteAccountRequest request
    ) {
        String password = request == null ? null : request.getPassword();
        AccountDeletionResult result =
                accountService.deleteAccount(accountId, password);

        HttpStatus status = switch (result.status()) {
            case DELETED -> HttpStatus.OK;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case INVALID_PASSWORD -> HttpStatus.FORBIDDEN;
            case HAS_EVENTS, RELATED_RECORDS -> HttpStatus.CONFLICT;
            case EVENT_SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        return ResponseEntity.status(status).body(result);
    }
}
