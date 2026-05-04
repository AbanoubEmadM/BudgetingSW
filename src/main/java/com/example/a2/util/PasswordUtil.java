package com.example.a2.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Static helpers for hashing and verifying passwords using SHA-256 and Base64 encoding.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.service.AuthenticationManager
 */
public class PasswordUtil {

    /**
     * Computes a Base64-encoded SHA-256 digest of the UTF-8 password bytes.
     *
     * @param password plain-text password
     * @return encoded hash string suitable for persistence
     * @throws RuntimeException wrapping {@link NoSuchAlgorithmException} if SHA-256 is unavailable
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a password by comparing its hash to the stored hash.
     *
     * @param password       user input
     * @param hashedPassword value from the database
     * @return {@code true} if hashes match
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(hashedPassword);
    }
}
