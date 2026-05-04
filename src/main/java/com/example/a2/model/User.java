package com.example.a2.model;

import java.time.LocalDateTime;

/**
 * Domain model for an application user stored in the {@code users} table.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.UserDAO
 * @see com.example.a2.service.AuthenticationManager
 */
public class User {

    /** Primary key from SQLite. */
    private int id;
    /** Unique login email. */
    private String email;
    /** Base64-encoded SHA-256 hash of the password. */
    private String passwordHash;
    /** Account creation timestamp from the database. */
    private LocalDateTime createdAt;

    /**
     * Constructs an empty user for ORM-style population.
     */
    public User() {}

    /**
     * Constructs a user with all scalar fields set.
     *
     * @param id            user id
     * @param email         email address
     * @param passwordHash  stored hash
     * @param createdAt     creation time
     */
    public User(int id, String email, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    /**
     * Returns the persistent user id.
     *
     * @return database primary key
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the persistent user id.
     *
     * @param id database primary key
     * @return nothing
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the user's email address.
     *
     * @return email string
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email new email value
     * @return nothing
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the stored password hash.
     *
     * @return hash string from persistence
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the stored password hash.
     *
     * @param passwordHash hash produced by {@link com.example.a2.util.PasswordUtil}
     * @return nothing
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the account creation time.
     *
     * @return creation timestamp, or {@code null} if not loaded
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the account creation time.
     *
     * @param createdAt creation timestamp
     * @return nothing
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
