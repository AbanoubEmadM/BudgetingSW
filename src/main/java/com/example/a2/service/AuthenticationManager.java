package com.example.a2.service;

import com.example.a2.dao.UserDAO;
import com.example.a2.model.User;
import com.example.a2.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Singleton managing registration, login, logout, and the current {@link User} session.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.UserDAO
 * @see com.example.a2.ui.LoginController
 */
public class AuthenticationManager {

    /** Lazily initialized singleton instance. */
    private static AuthenticationManager instance;
    /** Persists and loads user rows. */
    private final UserDAO userDAO;
    /** Currently authenticated user, or {@code null} if logged out. */
    private User currentUser;

    /**
     * Private constructor for singleton pattern.
     */
    private AuthenticationManager() {
        this.userDAO = new UserDAO();
    }

    /**
     * Returns the global {@link AuthenticationManager} instance (thread-safe lazy init).
     *
     * @return singleton instance
     */
    public static synchronized AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }

    /**
     * Registers a new user if the email is not already taken.
     *
     * @param email    unique login email
     * @param password plain password (hashed before insert)
     * @return {@code true} if registration succeeded
     * @throws SQLException if persistence fails
     */
    public boolean register(String email, String password) throws SQLException {
        Optional<User> existingUser = userDAO.findByEmail(email);
        if (existingUser.isPresent()) {
            return false;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);
        userDAO.createUser(email, hashedPassword);
        return true;
    }

    /**
     * Validates credentials and sets {@link #currentUser} on success.
     *
     * @param email    login email
     * @param password plain password to verify
     * @return {@code true} if login succeeded
     * @throws SQLException if the lookup fails
     */
    public boolean login(String email, String password) throws SQLException {
        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            currentUser = user;
            return true;
        }

        return false;
    }

    /**
     * Clears the current session user.
     *
     * @return nothing
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Returns the logged-in user, if any.
     *
     * @return current user or {@code null}
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns whether a user is currently authenticated.
     *
     * @return {@code true} if {@link #currentUser} is non-null
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
