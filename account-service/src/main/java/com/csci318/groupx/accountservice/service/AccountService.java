package com.csci318.groupx.accountservice.service;

import com.csci318.groupx.accountservice.domain.model.Account;
import com.csci318.groupx.accountservice.domain.model.AccountRole;
import com.csci318.groupx.accountservice.infrastructure.repository.AccountRepository;
import com.csci318.groupx.accountservice.messaging.AccountEventPublisher;
import com.csci318.groupx.accountservice.presentation.dto.AccountProfileResponse;
import com.csci318.groupx.accountservice.presentation.dto.AuthResponse;
import com.csci318.groupx.accountservice.presentation.dto.ChangePasswordRequest;
import com.csci318.groupx.accountservice.presentation.dto.LoginRequest;
import com.csci318.groupx.accountservice.presentation.dto.PasswordChangeResponse;
import com.csci318.groupx.accountservice.presentation.dto.SignupRequest;
import com.csci318.groupx.accountservice.presentation.dto.UpdateAccountRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;

@Service
public class AccountService {

    private static final int MAX_PROFILE_PICTURE_BYTES = 5 * 1024 * 1024;
    private static final int MAX_PROFILE_PICTURE_DIMENSION = 4096;

    private static final Logger logger =
            LoggerFactory.getLogger(AccountService.class);

    // The event service must be reachable before account deletion is allowed,
    // so published events cannot be left without an owner.
    private static final RestClient EVENT_CLIENT =
            RestClient.create("http://localhost:8081");

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

    // ===========================
    // Account Registration
    // ===========================

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

        String username = request.getUsername().trim();

        String email = request.getEmail()
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

        // Validate the selected account role.
        AccountRole role;

        try {
            String requestedRole = request.getRole();

            role = requestedRole == null
                    || requestedRole.isBlank()
                    ? AccountRole.GOER
                    : AccountRole.valueOf(
                    requestedRole
                            .trim()
                            .toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException exception) {

            logger.warn(
                    "Signup rejected because account role was invalid."
            );

            databaseLogService.warn(
                    "SIGNUP_FAILED",
                    null,
                    "Signup rejected because account role was invalid."
            );

            return new AuthResponse(
                    false,
                    "Invalid account role. Use GOER or MANAGER.",
                    null,
                    null,
                    null
            );
        }

        // Hash the password before storing it.
        String passwordHash =
                passwordHasher.hash(
                        request.getPassword()
                );

        Account account = new Account(
                username,
                email,
                passwordHash
        );

        // Store the account role in the database.
        account.setRole(role);

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
        accountEventPublisher.accountCreated(savedAccount.getId());

        return new AuthResponse(
                true,
                "Signup successful.",
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmail(),
                savedAccount.getRole().name()
        );
    }

    // ===========================
    // Account Login
    // ===========================

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

        Account account = accountOptional.get();

        // Verify the hashed password.
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

        // Return role information after successful login.
        return new AuthResponse(
                true,
                "Login successful.",
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getRole().name()
        );
    }

    // ===========================
    // Get Account Profile
    // ===========================

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
                accountRepository.findById(accountId);

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

        Account account = accountOptional.get();

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

    public Optional<Account> getAccountWithProfilePicture(Long accountId) {
        if (accountId == null) {
            return Optional.empty();
        }
        return accountRepository.findById(accountId)
                .filter(account -> account.getProfilePicture() != null);
    }

    public boolean updateAccountProfilePicture(
            Long accountId, byte[] picture, String contentType
    ) {
        if (accountId == null) {
            throw new IllegalArgumentException("Invalid account ID.");
        }
        validateProfilePicture(picture, contentType);

        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isEmpty()) {
            return false;
        }

        Account account = accountOptional.get();
        account.setProfilePicture(picture);
        account.setProfilePictureContentType(contentType.toLowerCase(Locale.ROOT));
        accountRepository.save(account);
        databaseLogService.info(
                "ACCOUNT_PROFILE_PICTURE_UPDATED",
                accountId,
                "Account profile picture updated successfully."
        );
        return true;
    }

    private void validateProfilePicture(byte[] picture, String contentType) {
        if (picture == null || picture.length == 0
                || picture.length > MAX_PROFILE_PICTURE_BYTES) {
            throw new IllegalArgumentException(
                    "Choose an image no larger than 5 MB."
            );
        }

        String normalizedContentType = contentType == null
                ? ""
                : contentType.toLowerCase(Locale.ROOT);
        if (!List.of("image/png", "image/jpeg", "image/gif")
                .contains(normalizedContentType)) {
            throw new IllegalArgumentException(
                    "Only PNG, JPEG, and GIF images are supported."
            );
        }

        try (ImageInputStream imageInput = ImageIO.createImageInputStream(
                new ByteArrayInputStream(picture))) {
            if (imageInput == null) {
                throw new IllegalArgumentException("The selected file is not a valid image.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("The selected file is not a valid image.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                boolean matchesContentType = switch (normalizedContentType) {
                    case "image/png" -> format.equals("png");
                    case "image/jpeg" -> format.equals("jpeg") || format.equals("jpg");
                    case "image/gif" -> format.equals("gif");
                    default -> false;
                };
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (!matchesContentType
                        || width <= 0 || height <= 0
                        || width > MAX_PROFILE_PICTURE_DIMENSION
                        || height > MAX_PROFILE_PICTURE_DIMENSION) {
                    throw new IllegalArgumentException(
                            "Choose a valid PNG, JPEG, or GIF image no larger than 4096 by 4096 pixels."
                    );
                }
                reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("The selected file is not a valid image.", exception);
        }
    }

    // ===========================
    // Update Account Profile
    // ===========================

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
                accountRepository.findById(accountId);

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

        Account account = accountOptional.get();

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
                accountRepository.findByUsername(username);

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
                accountRepository.findByEmail(email);

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
        accountEventPublisher.accountUpdated(savedAccount.getId());

        return new AccountProfileResponse(
                true,
                "Account profile updated.",
                savedAccount.getId(),
                savedAccount.getUsername(),
                savedAccount.getEmail(),
                savedAccount.getLocation()
        );
    }

    // ===========================
    // Delete Account
    // ===========================

    public PasswordChangeResponse changePassword(
            Long accountId,
            ChangePasswordRequest request
    ) {
        if (accountId == null
                || request == null
                || isBlank(request.getCurrentPassword())
                || isBlank(request.getNewPassword())) {
            databaseLogService.warn(
                    "PASSWORD_CHANGE_FAILED",
                    accountId,
                    "Password change rejected because required fields were missing."
            );
            return new PasswordChangeResponse(
                    false,
                    "Current password and new password are required."
            );
        }

        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isEmpty()) {
            databaseLogService.warn(
                    "PASSWORD_CHANGE_FAILED",
                    accountId,
                    "Password change rejected because the account was not found."
            );
            return new PasswordChangeResponse(false, "Account not found.");
        }

        Account account = accountOptional.get();
        if (!passwordHasher.matches(
                request.getCurrentPassword(),
                account.getPasswordHash())) {
            databaseLogService.warn(
                    "PASSWORD_CHANGE_FAILED",
                    accountId,
                    "Password change rejected because the current password was incorrect."
            );
            return new PasswordChangeResponse(false, "Current password is incorrect.");
        }

        account.setPasswordHash(passwordHasher.hash(request.getNewPassword()));
        accountRepository.save(account);
        databaseLogService.info(
                "PASSWORD_CHANGED",
                accountId,
                "Account password changed successfully."
        );
        return new PasswordChangeResponse(true, "Password changed successfully.");
    }

    public AccountDeletionResult deleteAccount(Long accountId, String password) {
        if (accountId == null || isBlank(password)) {
            databaseLogService.warn(
                    "ACCOUNT_DELETE_FAILED",
                    accountId,
                    "Deletion rejected because account ID or password was missing."
            );
            return new AccountDeletionResult(
                    false,
                    "Your current password is required.",
                    AccountDeletionResult.Status.BAD_REQUEST
            );
        }

        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isEmpty()) {
            databaseLogService.warn(
                    "ACCOUNT_DELETE_FAILED",
                    accountId,
                    "Deletion rejected because the account was not found."
            );
            return new AccountDeletionResult(
                    false,
                    "Account not found.",
                    AccountDeletionResult.Status.NOT_FOUND
            );
        }

        Account account = accountOptional.get();
        if (!passwordHasher.matches(password, account.getPasswordHash())) {
            databaseLogService.warn(
                    "ACCOUNT_DELETE_FAILED",
                    accountId,
                    "Deletion rejected because the password was invalid."
            );
            return new AccountDeletionResult(
                    false,
                    "Incorrect password. Account was not deleted.",
                    AccountDeletionResult.Status.INVALID_PASSWORD
            );
        }

        // Refuse deletion when the event service cannot confirm ownership.
        // We do not delete other microservices' data implicitly.
        try {
            if (hasOwnedEvents(accountId)) {
                databaseLogService.warn(
                        "ACCOUNT_DELETE_FAILED",
                        accountId,
                        "Deletion blocked because account still owns events."
                );
                return new AccountDeletionResult(
                        false,
                        "Delete your published events before deleting your account.",
                        AccountDeletionResult.Status.HAS_EVENTS
                );
            }
        } catch (RuntimeException exception) {
            logger.warn(
                    "Account deletion could not verify event ownership. accountId={}",
                    accountId,
                    exception
            );
            databaseLogService.warn(
                    "ACCOUNT_DELETE_FAILED",
                    accountId,
                    "Deletion blocked because the event service was unavailable."
            );
            return new AccountDeletionResult(
                    false,
                    "The event service is unavailable. Please try again when it is running.",
                    AccountDeletionResult.Status.EVENT_SERVICE_UNAVAILABLE
            );
        }

        try {
            accountRepository.delete(account);
            accountRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            logger.warn(
                    "Account deletion blocked by database references. accountId={}",
                    accountId,
                    exception
            );
            databaseLogService.warn(
                    "ACCOUNT_DELETE_FAILED",
                    accountId,
                    "Deletion blocked by related database records."
            );
            return new AccountDeletionResult(
                    false,
                    "This account has related records that must be removed first.",
                    AccountDeletionResult.Status.RELATED_RECORDS
            );
        }

        // Log only the account ID, not the deleted profile or password.
        databaseLogService.info(
                "ACCOUNT_DELETED",
                accountId,
                "Account deleted successfully."
        );
        logger.info("Account deleted. accountId={}", accountId);
        accountEventPublisher.accountDeleted(accountId);

        return new AccountDeletionResult(
                true,
                "Account deleted successfully.",
                AccountDeletionResult.Status.DELETED
        );
    }

    private boolean hasOwnedEvents(Long accountId) {
        List<Map<String, Object>> events = EVENT_CLIENT.get()
                .uri("/api/events")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        if (events == null) {
            throw new IllegalStateException("Empty event ownership response.");
        }

        for (Map<String, Object> event : events) {
            Object owner = event.get("ownerId");
            if (owner instanceof Number number
                    && number.longValue() == accountId.longValue()) {
                return true;
            }
        }
        return false;
    }

    // ===========================
    // Find Account By ID
    // ===========================

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

    // ===========================
    // Helper Methods
    // ===========================

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