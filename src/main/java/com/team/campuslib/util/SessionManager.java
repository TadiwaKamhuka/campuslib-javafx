package com.team.campuslib.util;

import com.team.campuslib.model.Role;
import com.team.campuslib.model.User;

public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }

    public void requireLoggedIn() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Please log in first.");
        }
    }

    public void requireAdmin() {
        requireLoggedIn();

        if (!isAdmin()) {
            throw new IllegalStateException(
                    "Access denied: administrator rights required.");
        }
    }
}
