package com.example.a2.service;

import com.example.a2.dao.GoalDAO;
import com.example.a2.dao.TransactionDAO;
import com.example.a2.model.Goal;
import com.example.a2.model.Transaction;
import com.example.a2.model.TransactionType;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Business rules, validation, and projections for financial goals.
 */
public class GoalService {

    private final GoalDAO goalDAO;
    private final TransactionDAO transactionDAO;

    public GoalService() {
        this.goalDAO = new GoalDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public GoalService(GoalDAO goalDAO, TransactionDAO transactionDAO) {
        this.goalDAO = goalDAO;
        this.transactionDAO = transactionDAO;
    }

    public List<Goal> loadGoalsForUser(int userId) throws SQLException {
        return goalDAO.findByUserId(userId);
    }

    public Goal createGoal(int userId, String name, double targetAmount, LocalDate deadline,
                           Double initialSavedAmount) throws SQLException {
        validateName(name);
        validateTargetAmount(targetAmount);
        validateDeadlineFuture(deadline);

        double initial = initialSavedAmount != null ? initialSavedAmount : 0;
        if (initial < 0) {
            throw new IllegalArgumentException("Initial saved amount cannot be negative");
        }

        Goal goal = new Goal(userId, name.trim(), targetAmount, 0, deadline);
        goalDAO.insertGoal(goal);

        if (initial > 0) {
            goalDAO.addContribution(goal.getId(), userId, initial, null);
            Goal refreshed = goalDAO.findById(goal.getId(), userId);
            return refreshed != null ? refreshed : goal;
        }
        return goal;
    }

    public void updateGoal(Goal existing, String name, double targetAmount, LocalDate deadline) throws SQLException {
        Objects.requireNonNull(existing, "goal");
        validateName(name);
        validateTargetAmount(targetAmount);
        if (deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }
        if (targetAmount < existing.getSavedAmount()) {
            throw new IllegalArgumentException("Target cannot be less than amount already saved");
        }

        existing.setName(name.trim());
        existing.setTargetAmount(targetAmount);
        existing.setDeadline(deadline);
        goalDAO.updateGoal(existing);
    }

    public void deleteGoal(int goalId, int userId) throws SQLException {
        goalDAO.deleteGoal(goalId, userId);
    }

    public void addManualContribution(int goalId, int userId, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Contribution must be greater than 0");
        }
        goalDAO.addContribution(goalId, userId, amount, null);
    }

    /**
     * Links an income transaction to a goal: applies the full transaction amount once.
     */
    public void addContributionFromTransaction(int goalId, int userId, int transactionId) throws SQLException {
        Transaction tx = transactionDAO.findById(transactionId);
        if (tx == null) {
            throw new IllegalArgumentException("Transaction not found");
        }
        if (tx.getUserId() != userId) {
            throw new IllegalArgumentException("Transaction does not belong to this user");
        }
        if (tx.getType() != TransactionType.INCOME) {
            throw new IllegalArgumentException("Only income transactions can fund a goal");
        }
        if (goalDAO.isTransactionLinkedToAnyGoal(transactionId)) {
            throw new IllegalArgumentException("This transaction is already linked to a goal");
        }

        goalDAO.addContribution(goalId, userId, tx.getAmount(), transactionId);
    }

    public Goal refreshGoal(int goalId, int userId) throws SQLException {
        return goalDAO.findById(goalId, userId);
    }

    public List<Transaction> listLinkableIncomeTransactions(int userId) throws SQLException {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactionDAO.getTransactionsByUser(userId)) {
            if (t.getType() == TransactionType.INCOME && !goalDAO.isTransactionLinkedToAnyGoal(t.getId())) {
                result.add(t);
            }
        }
        return result;
    }

    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name is required");
        }
    }

    public static void validateTargetAmount(double targetAmount) {
        if (targetAmount <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than 0");
        }
    }

    /** New goals: deadline must be strictly in the future (after today). */
    public static void validateDeadlineFuture(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline is required");
        }
        if (!deadline.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }
    }

    /**
     * Remaining amount toward the target (never negative).
     */
    public static double remainingAmount(double savedAmount, double targetAmount) {
        return Math.max(0, targetAmount - savedAmount);
    }

    /**
     * Monthly savings estimate: remaining divided by fractional months until deadline.
     * If the deadline is today or in the past, returns the full remaining amount (catch-up).
     */
    public static double monthlySavingsNeeded(double savedAmount, double targetAmount, LocalDate deadline) {
        double remaining = remainingAmount(savedAmount, targetAmount);
        if (remaining <= 0 || deadline == null) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, deadline);
        if (days <= 0) {
            return remaining;
        }
        double months = Math.max(days / 30.0, 1.0 / 30.0);
        return remaining / months;
    }
}
