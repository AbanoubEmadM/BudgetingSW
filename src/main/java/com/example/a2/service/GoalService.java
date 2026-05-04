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
 * Business rules, validation, and projections for user {@link Goal} entities.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.GoalDAO
 * @see com.example.a2.ui.GoalController
 */
public class GoalService {

    /** Goal persistence. */
    private final GoalDAO goalDAO;
    /** Used to validate and read transactions when linking contributions. */
    private final TransactionDAO transactionDAO;

    /**
     * Constructs the service with default DAO instances.
     */
    public GoalService() {
        this.goalDAO = new GoalDAO();
        this.transactionDAO = new TransactionDAO();
    }

    /**
     * Constructs the service with injected DAOs (testing / DI).
     *
     * @param goalDAO         goal persistence
     * @param transactionDAO  transaction persistence
     */
    public GoalService(GoalDAO goalDAO, TransactionDAO transactionDAO) {
        this.goalDAO = goalDAO;
        this.transactionDAO = transactionDAO;
    }

    /**
     * Loads all goals for a user from persistence.
     *
     * @param userId owning user
     * @return ordered goal list
     * @throws SQLException on query failure
     */
    public List<Goal> loadGoalsForUser(int userId) throws SQLException {
        return goalDAO.findByUserId(userId);
    }

    /**
     * Validates inputs, inserts the goal, and optionally records an initial contribution.
     *
     * @param userId             owning user
     * @param name               goal title
     * @param targetAmount       savings target
     * @param deadline           must be strictly after today
     * @param initialSavedAmount optional starting balance, or {@code null} for zero
     * @return persisted goal (refreshed if initial contribution applied)
     * @throws SQLException            on persistence failure
     * @throws IllegalArgumentException if validation fails
     */
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

    /**
     * Updates an existing goal after validating target vs saved and deadline rules.
     *
     * @param existing      goal row to mutate and persist
     * @param name          new title
     * @param targetAmount  new target (must be &gt;= saved)
     * @param deadline      new deadline (not before today)
     * @return nothing
     * @throws SQLException             on update failure
     * @throws IllegalArgumentException if validation fails
     */
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

    /**
     * Deletes a goal owned by the user.
     *
     * @param goalId goal id
     * @param userId owning user
     * @return nothing
     * @throws SQLException on delete failure
     */
    public void deleteGoal(int goalId, int userId) throws SQLException {
        goalDAO.deleteGoal(goalId, userId);
    }

    /**
     * Adds a manual contribution toward a goal.
     *
     * @param goalId goal id
     * @param userId owning user
     * @param amount positive increment
     * @return nothing
     * @throws SQLException             on persistence failure
     * @throws IllegalArgumentException if amount is not positive
     */
    public void addManualContribution(int goalId, int userId, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Contribution must be greater than 0");
        }
        goalDAO.addContribution(goalId, userId, amount, null);
    }

    /**
     * Links an income transaction to a goal, applying the full transaction amount once.
     *
     * @param goalId         goal id
     * @param userId         owning user
     * @param transactionId  income transaction id not yet linked
     * @return nothing
     * @throws SQLException             on persistence failure
     * @throws IllegalArgumentException if the transaction is invalid or already linked
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

    /**
     * Reloads a single goal from the database.
     *
     * @param goalId goal id
     * @param userId owning user
     * @return goal or {@code null}
     * @throws SQLException on query failure
     */
    public Goal refreshGoal(int goalId, int userId) throws SQLException {
        return goalDAO.findById(goalId, userId);
    }

    /**
     * Lists income transactions for the user that are not yet linked to any goal.
     *
     * @param userId owning user
     * @return linkable income rows
     * @throws SQLException on query failure
     */
    public List<Transaction> listLinkableIncomeTransactions(int userId) throws SQLException {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactionDAO.getTransactionsByUser(userId)) {
            if (t.getType() == TransactionType.INCOME && !goalDAO.isTransactionLinkedToAnyGoal(t.getId())) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Validates that a goal name is non-blank.
     *
     * @param name raw name input
     * @return nothing
     * @throws IllegalArgumentException if empty or whitespace only
     */
    public static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name is required");
        }
    }

    /**
     * Validates that the target amount is positive.
     *
     * @param targetAmount proposed target
     * @return nothing
     * @throws IllegalArgumentException if not greater than zero
     */
    public static void validateTargetAmount(double targetAmount) {
        if (targetAmount <= 0) {
            throw new IllegalArgumentException("Target amount must be greater than 0");
        }
    }

    /**
     * Validates that a new goal's deadline is strictly after today.
     *
     * @param deadline proposed deadline
     * @return nothing
     * @throws IllegalArgumentException if null or not in the future
     */
    public static void validateDeadlineFuture(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Deadline is required");
        }
        if (!deadline.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Deadline must be in the future");
        }
    }

    /**
     * Computes remaining amount toward the target (never negative).
     *
     * @param savedAmount  current saved total
     * @param targetAmount goal target
     * @return {@code max(0, target - saved)}
     */
    public static double remainingAmount(double savedAmount, double targetAmount) {
        return Math.max(0, targetAmount - savedAmount);
    }

    /**
     * Estimates monthly savings needed as remaining divided by fractional months until deadline.
     * If the deadline is not after today, returns the full remaining amount as catch-up.
     *
     * @param savedAmount  current saved total
     * @param targetAmount goal target
     * @param deadline     goal deadline date
     * @return suggested monthly savings rate
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
