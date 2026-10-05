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

    public AccountService(
            AccountRepository accountRepository,
            PasswordHasher passwordHasher
    ) {
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
    }

    public AuthResponse signup(SignupRequest request) {

        if (request == null
                || isBlank(request.getUsername())
                || isBlank(request.getEmail())
                || isBlank(request.getPassword())) {

            logger.warn(
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

        String username = request.getUsername().trim();
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (accountRepository.existsByUsername(username)) {

            logger.warn(
                    "Signup rejected because username '{}' already exists.",
                    username
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

            return new AuthResponse(
                    false,
                    "Email already exists.",
                    null,
                    null,
                    null
            );
        }

        String passwordHash =
                passwordHasher.hash(request.getPassword());

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
                    accountRepository.findByUsername(identifier);
        }

        if (accountOptional.isEmpty()) {

            logger.warn(
                    "Login failed for identifier '{}': account not found.",
                    identifier
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

            return invalidLoginResponse();
        }

        logger.info(
                "Login successful. accountId={}, username={}",
                account.getId(),
                account.getUsername()
        );

        return new AuthResponse(
                true,
                "Login successful.",
                account.getId(),
                account.getUsername(),
                account.getEmail()
        );
    }

    /*
     * Loads the full account profile from the database.
     */
    public AccountProfileResponse getAccountProfile(
            Long accountId
    ) {

        if (accountId == null) {

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
                    "Account profile access failed. accountId={} not found.",
                    accountId
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

        return new AccountProfileResponse(
                true,
                "Account profile loaded.",
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getLocation()
        );
    }

    /*
     * Updates username, email and location for an account.
     */
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
                accountRepository.findById(accountId);

        if (accountOptional.isEmpty()) {

            logger.warn(
                    "Account profile update failed. accountId={} not found.",
                    accountId
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

        /*
         * Prevent another account from using the same username.
         */
        Optional<Account> usernameOwner =
                accountRepository.findByUsername(username);

        if (usernameOwner.isPresent()
                && !usernameOwner.get()
                .getId()
                .equals(accountId)) {

            logger.warn(
                    "Account update rejected. Username '{}' already belongs to another account.",
                    username
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

        /*
         * Prevent another account from using the same email.
         */
        Optional<Account> emailOwner =
                accountRepository.findByEmail(email);

        if (emailOwner.isPresent()
                && !emailOwner.get()
                .getId()
                .equals(accountId)) {

            logger.warn(
                    "Account update rejected. Email '{}' already belongs to another account.",
                    email
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
                location == null || location.isBlank()
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

        return new AccountProfileResponse(
                true,
                "Account profile updated.",
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmail(),
                savedAccount.getLocation()
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

        return accountRepository.findById(accountId);
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
        return value == null || value.isBlank();
    }
}