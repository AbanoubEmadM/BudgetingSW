package com.example.a2.service;

import com.example.a2.dao.UserDAO;
import com.example.a2.model.User;
import com.example.a2.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Singleton - AuthenticationManager manages user authentication
 */
public class AuthenticationManager {
    private static AuthenticationManager instance;
    private final UserDAO userDAO;
    private User currentUser;

    // restrict instantiation to singleton
    private AuthenticationManager() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthenticationManager getInstance() {
        if (instance == null) {
            instance = new AuthenticationManager();
        }
        return instance;
    }

    public boolean register(String email, String password) throws SQLException {
        // Check if user already exists
        Optional<User> existingUser = userDAO.findByEmail(email);
        if (existingUser.isPresent()) {
            return false;
        }

        // Hash password and create user
        String hashedPassword = PasswordUtil.hashPassword(password);
        userDAO.createUser(email, hashedPassword);
        return true;
    }

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

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
