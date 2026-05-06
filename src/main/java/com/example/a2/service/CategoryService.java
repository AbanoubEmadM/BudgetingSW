package com.example.a2.service;

import com.example.a2.dao.CategoryDAO;
import com.example.a2.model.Category;
import com.example.a2.model.TransactionType;

import java.sql.SQLException;
import java.util.List;

/**
 * Thin service over {@link CategoryDAO} for loading categories into JavaFX controls.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.CategoryDAO
 * @see com.example.a2.model.Category
 */
public class CategoryService {

    /** Category persistence. */
    private final CategoryDAO categoryDAO;

    /**
     * Constructs the service with a default DAO.
     */
    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    /**
     * Returns every category ordered by type and name.
     *
     * @return full category list
     * @throws SQLException on query failure
     */
    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }

    /**
     * Returns categories filtered by {@link TransactionType}.
     *
     * @param type INCOME or EXPENSE filter
     * @return matching categories
     * @throws SQLException on query failure
     */
    public List<Category> getCategoriesByType(TransactionType type) throws SQLException {
        return categoryDAO.getCategoriesByType(type);
    }
}
