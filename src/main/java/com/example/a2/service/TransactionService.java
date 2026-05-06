package com.example.a2.service;

import com.example.a2.dao.TransactionDAO;
import com.example.a2.model.Transaction;
import com.example.a2.util.NotificationSender;

import java.sql.SQLException;
import java.util.List;

/**
 * Coordinates transaction persistence and post-save budget checks for expenses.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.TransactionDAO
 * @see BudgetService
 */
public class TransactionService {

    /** Transaction persistence. */
    private final TransactionDAO transactionDAO;
    /** Used to evaluate alerts after expenses. */
    private final BudgetService budgetService;

    /**
     * Constructs the service with injected {@link BudgetService}.
     *
     * @param budgetService non-null budget service for alert checks
     */
    public TransactionService(BudgetService budgetService) {
        this.transactionDAO = new TransactionDAO();
        this.budgetService = budgetService;
    }

    /**
     * Persists a transaction and may trigger a budget alert for expense rows.
     *
     * @param transaction          entity to insert
     * @param notificationSender   channel for budget warnings
     * @return nothing
     * @throws SQLException on insert or alert lookup failure
     */
    public void addTransaction(Transaction transaction, NotificationSender notificationSender) throws SQLException {
        transactionDAO.createTransaction(transaction);

        if (transaction.getType().name().equals("EXPENSE")) {
            budgetService.checkBudgetAlert(
                transaction.getUserId(),
                transaction.getCategoryId(),
                transaction.getTransactionDate().getMonthValue(),
                transaction.getTransactionDate().getYear(),
                notificationSender
            );
        }
    }

    /**
     * Lists all transactions for a user.
     *
     * @param userId owning user
     * @return ordered transactions
     * @throws SQLException on query failure
     */
    public List<Transaction> getTransactionsByUser(int userId) throws SQLException {
        return transactionDAO.getTransactionsByUser(userId);
    }

    /**
     * Lists transactions in a calendar month.
     *
     * @param userId owning user
     * @param month  month 1–12
     * @param year   four-digit year
     * @return transactions in range
     * @throws SQLException on query failure
     */
    public List<Transaction> getMonthlyTransactions(int userId, int month, int year) throws SQLException {
        return transactionDAO.getTransactionsByUserAndMonth(userId, month, year);
    }

    /**
     * Deletes a transaction by id.
     *
     * @param transactionId primary key
     * @return nothing
     * @throws SQLException on delete failure
     */
    public void deleteTransaction(int transactionId) throws SQLException {
        transactionDAO.deleteTransaction(transactionId);
    }

    /**
     * Sums income amounts in the given month.
     *
     * @param userId owning user
     * @param month  calendar month
     * @param year   calendar year
     * @return total income
     * @throws SQLException on query failure
     */
    public double calculateMonthlyIncome(int userId, int month, int year) throws SQLException {
        return getMonthlyTransactions(userId, month, year).stream()
            .filter(t -> t.getType().name().equals("INCOME"))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }

    /**
     * Sums expense amounts in the given month.
     *
     * @param userId owning user
     * @param month  calendar month
     * @param year   calendar year
     * @return total expenses
     * @throws SQLException on query failure
     */
    public double calculateMonthlyExpense(int userId, int month, int year) throws SQLException {
        return getMonthlyTransactions(userId, month, year).stream()
            .filter(t -> t.getType().name().equals("EXPENSE"))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
}
