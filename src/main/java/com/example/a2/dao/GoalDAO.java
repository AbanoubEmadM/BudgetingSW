package com.example.a2.dao;

import com.example.a2.model.Goal;
import com.example.a2.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for {@code financial_goals} and {@code goal_contributions}.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.model.Goal
 * @see com.example.a2.util.DatabaseManager
 */
public class GoalDAO {

    /** Shared JDBC connection from {@link DatabaseManager}. */
    private final Connection connection;

    /**
     * Constructs the DAO with the singleton connection.
     */
    public GoalDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Inserts a goal and assigns the generated id to the entity.
     *
     * @param goal populated goal (id updated after insert)
     * @return the same {@link Goal} instance with id set
     * @throws SQLException on insert failure
     */
    public Goal insertGoal(Goal goal) throws SQLException {
        String sql = """
            INSERT INTO financial_goals (user_id, name, target_amount, saved_amount, deadline)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, goal.getUserId());
            pstmt.setString(2, goal.getName());
            pstmt.setDouble(3, goal.getTargetAmount());
            pstmt.setDouble(4, goal.getSavedAmount());
            pstmt.setString(5, goal.getDeadline().toString());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    goal.setId(keys.getInt(1));
                }
            }
        }
        return goal;
    }

    /**
     * Updates mutable goal fields for an existing row owned by the user.
     *
     * @param goal entity with id and userId set
     * @return nothing
     * @throws SQLException on update failure
     */
    public void updateGoal(Goal goal) throws SQLException {
        String sql = """
            UPDATE financial_goals
            SET name = ?, target_amount = ?, saved_amount = ?, deadline = ?
            WHERE id = ? AND user_id = ?
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, goal.getName());
            pstmt.setDouble(2, goal.getTargetAmount());
            pstmt.setDouble(3, goal.getSavedAmount());
            pstmt.setString(4, goal.getDeadline().toString());
            pstmt.setInt(5, goal.getId());
            pstmt.setInt(6, goal.getUserId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a goal owned by the given user (contributions cascade).
     *
     * @param goalId goal primary key
     * @param userId owning user (authorization)
     * @return nothing
     * @throws SQLException on delete failure
     */
    public void deleteGoal(int goalId, int userId) throws SQLException {
        String sql = "DELETE FROM financial_goals WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, goalId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Loads a goal by id if owned by the user.
     *
     * @param goalId goal id
     * @param userId owning user
     * @return goal or {@code null}
     * @throws SQLException on query failure
     */
    public Goal findById(int goalId, int userId) throws SQLException {
        String sql = "SELECT * FROM financial_goals WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, goalId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lists all goals for a user ordered by deadline.
     *
     * @param userId owning user
     * @return goals for UI binding
     * @throws SQLException on query failure
     */
    public List<Goal> findByUserId(int userId) throws SQLException {
        List<Goal> list = new ArrayList<>();
        String sql = """
            SELECT * FROM financial_goals
            WHERE user_id = ?
            ORDER BY deadline ASC, id ASC
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Records a contribution and increments {@code saved_amount} in one transaction.
     *
     * @param goalId         goal id
     * @param userId         owning user (authorization)
     * @param amount         positive contribution
     * @param transactionId  optional linked income transaction id, or {@code null} for manual
     * @return nothing
     * @throws SQLException if the update affects zero rows or insert fails
     */
    public void addContribution(int goalId, int userId, double amount, Integer transactionId) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String insertContrib = """
                INSERT INTO goal_contributions (goal_id, amount, transaction_id)
                VALUES (?, ?, ?)
                """;
            try (PreparedStatement pstmt = connection.prepareStatement(insertContrib)) {
                pstmt.setInt(1, goalId);
                pstmt.setDouble(2, amount);
                if (transactionId == null) {
                    pstmt.setNull(3, Types.INTEGER);
                } else {
                    pstmt.setInt(3, transactionId);
                }
                pstmt.executeUpdate();
            }

            String updateGoal = """
                UPDATE financial_goals
                SET saved_amount = saved_amount + ?
                WHERE id = ? AND user_id = ?
                """;
            try (PreparedStatement pstmt = connection.prepareStatement(updateGoal)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, goalId);
                pstmt.setInt(3, userId);
                int updated = pstmt.executeUpdate();
                if (updated != 1) {
                    throw new SQLException("Goal not found or access denied");
                }
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Returns whether an income transaction is already linked to any goal contribution.
     *
     * @param transactionId transaction primary key
     * @return {@code true} if a {@code goal_contributions} row references it
     * @throws SQLException on query failure
     */
    public boolean isTransactionLinkedToAnyGoal(int transactionId) throws SQLException {
        String sql = "SELECT 1 FROM goal_contributions WHERE transaction_id = ? LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Maps a {@code financial_goals} row to a {@link Goal}.
     *
     * @param rs current row
     * @return populated goal
     * @throws SQLException if columns are invalid
     */
    private Goal mapRow(ResultSet rs) throws SQLException {
        Goal g = new Goal();
        g.setId(rs.getInt("id"));
        g.setUserId(rs.getInt("user_id"));
        g.setName(rs.getString("name"));
        g.setTargetAmount(rs.getDouble("target_amount"));
        g.setSavedAmount(rs.getDouble("saved_amount"));
        g.setDeadline(LocalDate.parse(rs.getString("deadline")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            g.setCreatedAt(ts.toLocalDateTime());
        }
        return g;
    }
}
