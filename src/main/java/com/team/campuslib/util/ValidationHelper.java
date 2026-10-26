package com.team.campuslib.util;

public class ValidationHelper {

    public static String validateLogin(String username, String password) {
        if (username == null || username.isBlank()) {
            return "Username is required.";
        }

        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }

        return null;
    }
}