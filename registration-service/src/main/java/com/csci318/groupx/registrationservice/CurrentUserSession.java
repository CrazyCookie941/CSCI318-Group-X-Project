package com.csci318.groupx.registrationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CurrentUserSession {

    private static final Logger logger =
            LoggerFactory.getLogger(CurrentUserSession.class);

    private static Long accountId;
    private static String username;
    private static String email;
    private static String role;

    private CurrentUserSession() {
    }

    // Keep the existing three-argument method for compatibility with other screens.
    public static void login(Long newAccountId, String newUsername, String newEmail) {
        login(newAccountId, newUsername, newEmail, "GOER");
    }

    public static void login(
            Long newAccountId,
            String newUsername,
            String newEmail,
            String newRole
    ) {
        accountId = newAccountId;
        username = newUsername;
        email = newEmail;
        role = "MANAGER".equalsIgnoreCase(newRole) ? "MANAGER" : "GOER";

        logger.info("User session started. accountId={}, username={}, role={}",
                accountId, username, role);
    }

    public static void logout() {
        if (accountId != null) {
            logger.info("User session ended. accountId={}, username={}",
                    accountId, username);
        }
        accountId = null;
        username = null;
        email = null;
        role = null;
    }

    public static void updateProfile(String newUsername, String newEmail) {
        username = newUsername;
        email = newEmail;
        logger.info("Current user session profile updated. accountId={}, username={}",
                accountId, username);
    }

    public static boolean isLoggedIn() {
        return accountId != null;
    }

    public static boolean isManager() {
        return isLoggedIn() && "MANAGER".equals(role);
    }

    public static Long getAccountId() {
        return accountId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getEmail() {
        return email;
    }

    public static String getRole() {
        return role;
    }
}
