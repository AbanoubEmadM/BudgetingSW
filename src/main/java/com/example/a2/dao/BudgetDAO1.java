package com.example.a2.dao;

import com.example.a2.model.Budget;
import com.example.a2.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for {@code budgets}: upsert monthly limits and join category names and spent totals.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.model.Budget
 * @see TransactionDAO
 */
public class BudgetDAO {

    /** Shared JDBC connection from {@link DatabaseManager}. */
    private final Connection connection;
    /** Used to compute {@link Budget#setSpent(double)} from transactions. */
    private final TransactionDAO transactionDAO;

    /**
     * Constructs the DAO with a new {@link TransactionDAO} for spent aggregation.
     */
    public BudgetDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Inserts or updates a monthly budget row (SQLite {@code ON CONFLICT} upsert).
     *
     * @param budget user, category, month, year, and amount
     * @return nothing
     * @throws SQLException on persistence failure
     */
    public void createOrUpdateBudget(Budget budget) throws SQLException {
        String sql = """
            INSERT INTO budgets (user_id, category_id, amount, month, year)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(user_id, category_id, month, year)
            DO UPDATE SET amount = excluded.amount
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, budget.getUserId());
            pstmt.setInt(2, budget.getCategoryId());
            pstmt.setDouble(3, budget.getAmount());
            pstmt.setInt(4, budget.getMonth());
            pstmt.setInt(5, budget.getYear());
            pstmt.executeUpdate();
        }
    }

    /**
     * Lists all budgets for a user/month with spent totals filled in.
     *
     * @param userId owning user
     * @param month  calendar month
     * @param year   calendar year
     * @return budgets including {@link Budget#setSpent(double)}
     * @throws SQLException on query failure
     */
    public List<Budget> getBudgetsByUserAndMonth(int userId, int month, int year) throws SQLException {
        List<Budget> budgets = new ArrayList<>();
        String sql = """
            SELECT b.*, c.name as category_name
            FROM budgets b
            JOIN categories c ON b.category_id = c.id
            WHERE b.user_id = ? AND b.month = ? AND b.year = ?
            ORDER BY c.name
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Budget budget = mapResultSetToBudget(rs);
                    double spent = transactionDAO.getTotalSpentByCategory(
                            userId, budget.getCategoryId(), month, year
                    );
                    budget.setSpent(spent);
                    budgets.add(budget);
                }
            }
        }
        return budgets;
    }

    /**
     * Fetches a single budget cell if defined for the user/category/month.
     *
     * @param userId     owning user
     * @param categoryId category key
     * @param month      month
     * @param year       year
     * @return budget with spent, if a row exists
     * @throws SQLException on query failure
     */
    public Optional<Budget> getBudgetByCategoryAndMonth(int userId, int categoryId, int month, int year) throws SQLException {
        String sql = """
            SELECT b.*, c.name as category_name
            FROM budgets b
            JOIN categories c ON b.category_id = c.id
            WHERE b.user_id = ? AND b.category_id = ? AND b.month = ? AND b.year = ?
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, categoryId);
            pstmt.setInt(3, month);
            pstmt.setInt(4, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Budget budget = mapResultSetToBudget(rs);
                    double spent = transactionDAO.getTotalSpentByCategory(userId, categoryId, month, year);
                    budget.setSpent(spent);
                    return Optional.of(budget);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Maps a joined budgets/categories row to a {@link Budget} without spent (caller sets spent).
     *
     * @param rs current row
     * @return budget instance
     * @throws SQLException if columns are invalid
     */
    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getInt("id"));
        budget.setUserId(rs.getInt("user_id"));
        budget.setCategoryId(rs.getInt("category_id"));
        budget.setCategoryName(rs.getString("category_name"));
        budget.setAmount(rs.getDouble("amount"));
        budget.setMonth(rs.getInt("month"));
        budget.setYear(rs.getInt("year"));
        return budget;
    }
}
// kda budget