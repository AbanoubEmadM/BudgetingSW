package com.example.a2.service;

import com.example.a2.dao.BudgetDAO;
import com.example.a2.model.Budget;
import com.example.a2.util.AlertStrategy;
import com.example.a2.util.NotificationSender;
import com.example.a2.util.ThresholdAlertStrategy;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BudgetService {
    private final BudgetDAO budgetDAO;
    private final AlertStrategy alertStrategy;

    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        // Default: alert at 80% threshold
        this.alertStrategy = new ThresholdAlertStrategy(80.0);
    }

    public BudgetService(AlertStrategy alertStrategy) {
        this.budgetDAO = new BudgetDAO();
        this.alertStrategy = alertStrategy;
    }

    public void setBudget(int userId, int categoryId, double amount, int month, int year) throws SQLException {
        Budget budget = new Budget(userId, categoryId, amount, month, year);
        budgetDAO.createOrUpdateBudget(budget);
    }

    public List<Budget> getMonthlyBudgets(int userId, int month, int year) throws SQLException {
        return budgetDAO.getBudgetsByUserAndMonth(userId, month, year);
    }

    public void checkBudgetAlert(int userId, int categoryId, int month, int year,
                                 NotificationSender notificationSender) throws SQLException {
        Optional<Budget> budgetOpt = budgetDAO.getBudgetByCategoryAndMonth(userId, categoryId, month, year);

        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            if (alertStrategy.shouldAlert(budget)) {
                notificationSender.sendNotification("Budget Alert", alertStrategy.getMessage(budget));
            }
        }
    }

    public double getTotalBudget(int userId, int month, int year) throws SQLException {
        return getMonthlyBudgets(userId, month, year).stream()
            .mapToDouble(Budget::getAmount)
            .sum();
    }

    public double getTotalSpent(int userId, int month, int year) throws SQLException {
        return getMonthlyBudgets(userId, month, year).stream()
            .mapToDouble(Budget::getSpent)
            .sum();
    }
}
