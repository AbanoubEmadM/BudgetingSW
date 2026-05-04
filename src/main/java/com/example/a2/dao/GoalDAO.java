package com.example.a2.dao;

import com.example.a2.model.Goal;
import com.example.a2.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    private final Connection connection;

    public GoalDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

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

    public void deleteGoal(int goalId, int userId) throws SQLException {
        String sql = "DELETE FROM financial_goals WHERE id = ? AND user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, goalId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

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
     * Adds to {@code saved_amount} on the goal and records a contribution row.
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

    public boolean isTransactionLinkedToAnyGoal(int transactionId) throws SQLException {
        String sql = "SELECT 1 FROM goal_contributions WHERE transaction_id = ? LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

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
