package com.example.a2.dao;

import com.example.a2.model.User;
import com.example.a2.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data access for {@code users}: registration lookup and credential loading.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.model.User
 * @see com.example.a2.util.DatabaseManager
 */
public class UserDAO {

    /** Shared JDBC connection from {@link DatabaseManager}. */
    private final Connection connection;

    /**
     * Opens a DAO using the singleton database connection.
     *
     * @throws RuntimeException if {@link DatabaseManager} cannot supply a connection
     */
    public UserDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Inserts a new user with email and password hash.
     *
     * @param email         unique email
     * @param passwordHash  stored hash string
     * @return nothing
     * @throws SQLException if the insert fails
     */
    public void createUser(String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, passwordHash);
            pstmt.executeUpdate();
        }
    }

    /**
     * Finds a user by exact email match.
     *
     * @param email login email
     * @return present {@link User} if found, else empty
     * @throws SQLException on query failure
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a user by primary key.
     *
     * @param id user id
     * @return present {@link User} if found, else empty
     * @throws SQLException on query failure
     */
    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Maps a current {@link ResultSet} row to a {@link User} instance.
     *
     * @param rs positioned row from {@code users}
     * @return populated user
     * @throws SQLException if a column is missing or invalid
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            user.setCreatedAt(timestamp.toLocalDateTime());
        }

        return user;
    }
}
