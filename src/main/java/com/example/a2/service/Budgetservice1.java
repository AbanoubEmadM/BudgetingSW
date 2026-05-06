package com.example.a2.service;

import com.example.a2.dao.BudgetDAO;
import com.example.a2.model.Budget;
import com.example.a2.util.AlertStrategy;
import com.example.a2.util.NotificationSender;
import com.example.a2.util.ThresholdAlertStrategy;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Application service for monthly budgets, persistence, and threshold-based alerts.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.BudgetDAO
 * @see AlertStrategy
 */
public class BudgetService {

    /** Budget persistence layer. */
    private final BudgetDAO budgetDAO;
    /** Strategy used after expenses to decide whether to notify. */
    private final AlertStrategy alertStrategy;

    /**
     * Constructs the service with default {@link ThresholdAlertStrategy} at 80% usage.
     */
    public BudgetService() {
        this.budgetDAO = new BudgetDAO();
        this.alertStrategy = new ThresholdAlertStrategy(80.0);
    }

    /**
     * Constructs the service with a custom alert strategy (e.g. for testing).
     *
     * @param alertStrategy non-null strategy implementation
     */
    public BudgetService(AlertStrategy alertStrategy) {
        this.budgetDAO = new BudgetDAO();
        this.alertStrategy = alertStrategy;
    }

    /**
     * Creates or updates the budget amount for a user/category/month.
     *
     * @param userId     owning user
     * @param categoryId expense category
     * @param amount     budget cap
     * @param month      calendar month
     * @param year       calendar year
     * @return nothing
     * @throws SQLException on persistence failure
     */
    public void setBudget(int userId, int categoryId, double amount, int month, int year) throws SQLException {
        Budget budget = new Budget(userId, categoryId, amount, month, year);
        budgetDAO.createOrUpdateBudget(budget);
    }

    /**
     * Loads all budgets for the dashboard month with spent totals.
     *
     * @param userId owning user
     * @param month  calendar month
     * @param year   calendar year
     * @return list of budgets for tables
     * @throws SQLException on query failure
     */
    public List<Budget> getMonthlyBudgets(int userId, int month, int year) throws SQLException {
        return budgetDAO.getBudgetsByUserAndMonth(userId, month, year);
    }

    /**
     * Evaluates whether a budget alert should be sent after an expense (strategy + optional sender).
     *
     * @param userId               owning user
     * @param categoryId           affected category
     * @param month                calendar month of the expense
     * @param year                 calendar year of the expense
     * @param notificationSender   channel used when an alert fires
     * @return nothing
     * @throws SQLException if budget lookup fails
     */
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

    /**
     * Sums budget caps across categories for the month.
     *
     * @param userId owning user
     * @param month  calendar month
     * @param year   calendar year
     * @return total budgeted amount
     * @throws SQLException on query failure
     */
    public double getTotalBudget(int userId, int month, int year) throws SQLException {
        return getMonthlyBudgets(userId, month, year).stream()
                .mapToDouble(Budget::getAmount)
                .sum();
    }

    /**
     * Sums spent amounts across categories for the month (from joined budget rows).
     *
     * @param userId owning user
     * @param month  calendar month
     * @param year   calendar year
     * @return total spent across budgeted categories
     * @throws SQLException on query failure
     */
    public double getTotalSpent(int userId, int month, int year) throws SQLException {
        return getMonthlyBudgets(userId, month, year).stream()
                .mapToDouble(Budget::getSpent)
                .sum();
    }
}
