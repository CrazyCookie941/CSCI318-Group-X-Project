package com.csci318.groupx.registrationservice.service;

import com.csci318.groupx.registrationservice.domain.model.Account;
import com.csci318.groupx.registrationservice.infrastructure.repository.AccountRepository;
import com.csci318.groupx.registrationservice.presentation.dto.AccountProfileResponse;
import com.csci318.groupx.registrationservice.presentation.dto.AuthResponse;
import com.csci318.groupx.registrationservice.presentation.dto.LoginRequest;
import com.csci318.groupx.registrationservice.presentation.dto.SignupRequest;
import com.csci318.groupx.registrationservice.presentation.dto.UpdateAccountRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class AccountService {

    private static final Logger logger =
            LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final PasswordHasher passwordHasher;
    private final DatabaseLogService databaseLogService;
    private final AccountEventPublisher accountEventPublisher;

    public AccountService(
            AccountRepository accountRepository,
            PasswordHasher passwordHasher,
            DatabaseLogService databaseLogService,
            AccountEventPublisher accountEventPublisher
    ) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
        this.databaseLogService = databaseLogService;
        this.accountEventPublisher = accountEventPublisher;
    }

    public AuthResponse signup(SignupRequest request) {

        if (request == null
                || isBlank(request.getUsername())
                || isBlank(request.getEmail())
                || isBlank(request.getPassword())) {

            logger.warn(
                    "Signup rejected because required fields were missing."
            );

            databaseLogService.warn(
                    "SIGNUP_FAILED",
                    null,
                    "Signup rejected because required fields were missing."
            );

            return new AuthResponse(
                    false,
                    "Username, email and password are required.",
                    null,
                    null,
                    null
            );
        }

        String username =
                request.getUsername().trim();

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (accountRepository.existsByUsername(username)) {

            logger.warn(
                    "Signup rejected because username '{}' already exists.",
                    username
            );

            databaseLogService.warn(
                    "SIGNUP_FAILED",
                    null,
                    "Signup rejected because username already exists."
            );

            return new AuthResponse(
                    false,
                    "Username already exists.",
                    null,
                    null,
                    null
            );
        }

        if (accountRepository.existsByEmail(email)) {

            logger.warn(
                    "Signup rejected because email '{}' already exists.",
                    email
            );

            databaseLogService.warn(
                    "SIGNUP_FAILED",
                    null,
                    "Signup rejected because email already exists."
            );

            return new AuthResponse(
                    false,
                    "Email already exists.",
                    null,
                    null,
                    null
            );
        }

        String passwordHash =
                passwordHasher.hash(
                        request.getPassword()
                );

        Account account =
                new Account(
                        username,
                        email,
                        passwordHash
                );

        Account savedAccount =
                accountRepository.save(account);

        logger.info(
                "Account signup successful. accountId={}, username={}",
                savedAccount.getId(),
                savedAccount.getUsername()
        );

        databaseLogService.info(
                "ACCOUNT_SIGNUP",
                savedAccount.getId(),
                "Account created successfully."
        );

        accountEventPublisher.publishAccountEvent(
                "ACCOUNT_CREATED",
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmail()
        );

        return new AuthResponse(
                true,
                "Signup successful.",
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {

        if (request == null
                || isBlank(request.getIdentifier())
                || isBlank(request.getPassword())) {

            logger.warn(
                    "Login rejected because required fields were missing."
            );

            databaseLogService.warn(
                    "LOGIN_FAILED",
                    null,
                    "Login rejected because required fields were missing."
            );

            return invalidLoginResponse();
        }

        String identifier =
                request.getIdentifier().trim();

        Optional<Account> accountOptional;

        if (identifier.contains("@")) {

            accountOptional =
                    accountRepository.findByEmail(
                            identifier.toLowerCase(Locale.ROOT)
                    );

        } else {

            accountOptional =
                    accountRepository.findByUsername(
                            identifier
                    );
        }

        if (accountOptional.isEmpty()) {

            logger.warn(
                    "Login failed for identifier '{}': account not found.",
                    identifier
            );

            databaseLogService.warn(
                    "LOGIN_FAILED",
                    null,
                    "Login failed because the account was not found."
            );

            return invalidLoginResponse();
        }

        Account account =
                accountOptional.get();

        if (!passwordHasher.matches(
                request.getPassword(),
                account.getPasswordHash())) {

            logger.warn(
                    "Login failed for accountId={}: invalid password.",
                    account.getId()
            );

            databaseLogService.warn(
                    "LOGIN_FAILED",
                    account.getId(),
                    "Login failed because the password was invalid."
            );

            return invalidLoginResponse();
        }

        logger.info(
                "Login successful. accountId={}, username={}",
                account.getId(),
                account.getUsername()
        );

        databaseLogService.info(
                "LOGIN_SUCCESS",
                account.getId(),
                "Authentication successful."
        );

        accountEventPublisher.publishAccountEvent(
                "ACCOUNT_LOGGED_IN",
                account.getId(),
                account.getUsername(),
                account.getEmail()
        );

        return new AuthResponse(
                true,
                "Login successful.",
                account.getId(),
                account.getUsername(),
                account.getEmail()
        );
    }

    public AccountProfileResponse getAccountProfile(
            Long accountId
    ) {

        if (accountId == null) {

            databaseLogService.warn(
                    "ACCOUNT_PROFILE_ACCESS_FAILED",
                    null,
                    "Account profile access rejected because account ID was invalid."
            );

            return new AccountProfileResponse(
                    false,
                    "Invalid account ID.",
                    null,
                    null,
                    null,
                    null
            );
        }

        Optional<Account> accountOptional =
                accountRepository.findById(
                        accountId
                );

        if (accountOptional.isEmpty()) {

            logger.warn(
                    "Account profile access failed. accountId={} not found.",
                    accountId
            );

            databaseLogService.warn(
                    "ACCOUNT_PROFILE_ACCESS_FAILED",
                    accountId,
                    "Account profile could not be found."
            );

            return new AccountProfileResponse(
                    false,
                    "Account not found.",
                    null,
                    null,
                    null,
                    null
            );
        }

        Account account =
                accountOptional.get();

        logger.info(
                "Account profile accessed. accountId={}, username={}",
                account.getId(),
                account.getUsername()
        );

        databaseLogService.info(
                "ACCOUNT_PROFILE_ACCESSED",
                account.getId(),
                "Account profile loaded successfully."
        );

        return new AccountProfileResponse(
                true,
                "Account profile loaded.",
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getLocation()
        );
    }

    public AccountProfileResponse updateAccountProfile(
            Long accountId,
            UpdateAccountRequest request
    ) {

        if (accountId == null
                || request == null
                || isBlank(request.getUsername())
                || isBlank(request.getEmail())) {

            logger.warn(
                    "Account profile update rejected because required fields were missing."
            );

            databaseLogService.warn(
                    "ACCOUNT_PROFILE_UPDATE_FAILED",
                    accountId,
                    "Account profile update rejected because required fields were missing."
            );

            return new AccountProfileResponse(
                    false,
                    "Username and email are required.",
                    accountId,
                    null,
                    null,
                    null
            );
        }

        Optional<Account> accountOptional =
                accountRepository.findById(
                        accountId
                );

        if (accountOptional.isEmpty()) {

            logger.warn(
                    "Account profile update failed. accountId={} not found.",
                    accountId
            );

            databaseLogService.warn(
                    "ACCOUNT_PROFILE_UPDATE_FAILED",
                    accountId,
                    "Account profile could not be found."
            );

            return new AccountProfileResponse(
                    false,
                    "Account not found.",
                    null,
                    null,
                    null,
                    null
            );
        }

        Account account =
                accountOptional.get();

        String username =
                request.getUsername().trim();

        String email =
                request.getEmail()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        String location =
                request.getLocation() == null
                        ? null
                        : request.getLocation().trim();

        Optional<Account> usernameOwner =
                accountRepository.findByUsername(
                        username
                );

        if (usernameOwner.isPresent()
                && !usernameOwner.get()
                .getId()
                .equals(accountId)) {

            logger.warn(
                    "Account update rejected. Username '{}' already belongs to another account.",
                    username
            );

            databaseLogService.warn(
                    "ACCOUNT_PROFILE_UPDATE_FAILED",
                    accountId,
                    "Account update rejected because username already exists."
            );

            return new AccountProfileResponse(
                    false,
                    "Username already exists.",
                    accountId,
                    account.getUsername(),
                    account.getEmail(),
                    account.getLocation()
            );
        }

        Optional<Account> emailOwner =
                accountRepository.findByEmail(
                        email
                );

        if (emailOwner.isPresent()
                && !emailOwner.get()
                .getId()
                .equals(accountId)) {

            logger.warn(
                    "Account update rejected. Email '{}' already belongs to another account.",
                    email
            );

            databaseLogService.warn(
                    "ACCOUNT_PROFILE_UPDATE_FAILED",
                    accountId,
                    "Account update rejected because email already exists."
            );

            return new AccountProfileResponse(
                    false,
                    "Email already exists.",
                    accountId,
                    account.getUsername(),
                    account.getEmail(),
                    account.getLocation()
            );
        }

        account.setUsername(username);
        account.setEmail(email);

        account.setLocation(
                location == null
                        || location.isBlank()
                        ? null
                        : location
        );

        Account savedAccount =
                accountRepository.save(account);

        logger.info(
                "Account profile updated. accountId={}, username={}",
                savedAccount.getId(),
                savedAccount.getUsername()
        );

        databaseLogService.info(
                "ACCOUNT_PROFILE_UPDATED",
                savedAccount.getId(),
                "Account profile updated successfully."
        );

        accountEventPublisher.publishAccountEvent(
                "ACCOUNT_UPDATED",
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmail()
        );

        return new AccountProfileResponse(
                true,
                "Account profile updated.",
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmail(),
                savedAccount.getLocation()
        );
    }

    public AccountProfileResponse deleteAccount(Long accountId) {

        if (accountId == null) {
            databaseLogService.warn(
                    "ACCOUNT_DELETE_FAILED",
                    null,
                    "Account deletion rejected because account ID was invalid."
            );

            return new AccountProfileResponse(
                    false,
                    "Invalid account ID.",
                    null,
                    null,
                    null,
                    null
            );
        }

        Optional<Account> accountOptional =
                accountRepository.findById(accountId);

        if (accountOptional.isEmpty()) {
            logger.warn(
                    "Account deletion failed. accountId={} not found.",
                    accountId
            );

            databaseLogService.warn(
                    "ACCOUNT_DELETE_FAILED",
                    accountId,
                    "Account could not be found for deletion."
            );

            return new AccountProfileResponse(
                    false,
                    "Account not found.",
                    null,
                    null,
                    null,
                    null
            );
        }

        Account account = accountOptional.get();
        String username = account.getUsername();
        String email = account.getEmail();

        accountRepository.delete(account);

        logger.info(
                "Account deleted. accountId={}, username={}",
                accountId,
                username
        );

        databaseLogService.info(
                "ACCOUNT_DELETED",
                accountId,
                "Account deleted successfully."
        );

        accountEventPublisher.publishAccountEvent(
                "ACCOUNT_DELETED",
                accountId,
                username,
                email
        );

        return new AccountProfileResponse(
                true,
                "Account deleted.",
                accountId,
                username,
                email,
                account.getLocation()
        );
    }

    public Optional<Account> findAccountById(
            Long accountId
    ) {

        if (accountId == null) {
            return Optional.empty();
        }

        logger.debug(
                "Loading account data for accountId={}",
                accountId
        );

        return accountRepository.findById(
                accountId
        );
    }

    private AuthResponse invalidLoginResponse() {

        return new AuthResponse(
                false,
                "Invalid username/email or password.",
                null,
                null,
                null
        );
    }

    private boolean isBlank(String value) {
        return value == null
                || value.isBlank();
    }
}