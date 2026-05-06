package com.example.a2.service;

import com.example.a2.dao.CategoryDAO;
import com.example.a2.model.Category;
import com.example.a2.model.TransactionType;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {
    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }

    public List<Category> getCategoriesByType(TransactionType type) throws SQLException {
        return categoryDAO.getCategoriesByType(type);
    }
}
