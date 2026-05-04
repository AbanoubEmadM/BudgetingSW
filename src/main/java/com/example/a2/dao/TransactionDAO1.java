package com.example.a2.dao;

import com.example.a2.model.Transaction;
import com.example.a2.model.TransactionType;
import com.example.a2.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private final Connection connection;

    public TransactionDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }
    public void createTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, amount, type, category_id, transaction_date, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, transaction.getUserId());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getType().name());
            pstmt.setInt(4, transaction.getCategoryId());
            pstmt.setString(5, transaction.getTransactionDate().toString());
            pstmt.setString(6, transaction.getDescription());
            pstmt.executeUpdate();
        }
    }
    public List<Transaction> getTransactionsByUser(int userId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.*, c.name as category_name
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ?
            ORDER BY t.transaction_date DESC, t.created_at DESC
        """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    public void deleteTransaction(int transactionId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            pstmt.executeUpdate();
        }
    }
    public List<Transaction> getTransactionsByUserAndMonth(int userId, int month, int year) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT t.*, c.name as category_name
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.user_id = ?
            AND strftime('%m', t.transaction_date) = ?
            AND strftime('%Y', t.transaction_date) = ?
            ORDER BY t.transaction_date DESC
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, String.format("%02d", month));
            pstmt.setString(3, String.valueOf(year));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
    public double getTotalSpentByCategory(int userId, int categoryId, int month, int year) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total
            FROM transactions
            WHERE user_id = ? AND category_id = ?
            AND type = 'EXPENSE'
            AND strftime('%m', transaction_date) = ?
            AND strftime('%Y', transaction_date) = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, categoryId);
            pstmt.setString(3, String.format("%02d", month));
            pstmt.setString(4, String.valueOf(year));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0.0;
    }
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setUserId(rs.getInt("user_id"));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setType(TransactionType.valueOf(rs.getString("type")));
        transaction.setCategoryId(rs.getInt("category_id"));
        transaction.setCategoryName(rs.getString("category_name"));
        transaction.setTransactionDate(LocalDate.parse(rs.getString("transaction_date")));
        transaction.setDescription(rs.getString("description"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            transaction.setCreatedAt(timestamp.toLocalDateTime());
        }

        return transaction;
    }

}
