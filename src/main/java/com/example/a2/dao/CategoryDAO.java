package com.example.a2.dao;

import com.example.a2.model.Category;
import com.example.a2.model.TransactionType;
import com.example.a2.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for {@code categories}: default and filtered lists for UI pickers.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.model.Category
 * @see com.example.a2.util.DatabaseManager
 */
public class CategoryDAO {

    /** Shared JDBC connection from {@link DatabaseManager}. */
    private final Connection connection;

    /**
     * Constructs the DAO with the singleton connection.
     */
    public CategoryDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Loads all categories ordered by type and name.
     *
     * @return mutable list of categories
     * @throws SQLException on query failure
     */
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY type, name";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
        }
        return categories;
    }

    /**
     * Loads categories filtered by income or expense type.
     *
     * @param type INCOME or EXPENSE filter
     * @return categories of the requested type
     * @throws SQLException on query failure
     */
    public List<Category> getCategoriesByType(TransactionType type) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE type = ? ORDER BY name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapResultSetToCategory(rs));
                }
            }
        }
        return categories;
    }

    /**
     * Maps a {@link ResultSet} row to a {@link Category}.
     *
     * @param rs current row
     * @return populated category
     * @throws SQLException if columns are invalid
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setType(TransactionType.valueOf(rs.getString("type")));
        return category;
    }
}
