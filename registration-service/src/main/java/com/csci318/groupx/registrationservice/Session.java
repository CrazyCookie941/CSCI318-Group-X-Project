package com.csci318.groupx.registrationservice;

final class Session
{
    private static User current;

    private Session() {}

    static void login(User user) { current = user; }

    static void logout() { current = null; }

    static User current() { return current; }

    static boolean isManager() { return current != null && current.role() == Role.MANAGER; }
}