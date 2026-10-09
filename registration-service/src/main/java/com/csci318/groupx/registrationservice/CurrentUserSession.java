package com.csci318.groupx.registrationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CurrentUserSession {

    private static final Logger logger =
            LoggerFactory.getLogger(CurrentUserSession.class);

    private static Long accountId;
    private static String username;
    private static String email;

    private CurrentUserSession() {
    }

    public static void login(
            Long newAccountId,
            String newUsername,
            String newEmail
    ) {
        accountId = newAccountId;
        username = newUsername;
        email = newEmail;

        logger.info(
                "User session started. accountId={}, username={}",
                accountId,
                username
        );
    }

    public static void logout() {
        if (accountId != null) {
            logger.info(
                    "User session ended. accountId={}, username={}",
                    accountId,
                    username
            );
        }

        accountId = null;
        username = null;
        email = null;
    }

    public static void updateProfile(
            String newUsername,
            String newEmail
    ) {
        username = newUsername;
        email = newEmail;

        logger.info(
                "Current user session profile updated. accountId={}, username={}",
                accountId,
                username
        );
    }

    public static boolean isLoggedIn() {
        return accountId != null;
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
}