package com.example.a2.service;

import com.example.a2.dao.TransactionDAO;
import com.example.a2.model.Transaction;
import com.example.a2.util.NotificationSender;

import java.sql.SQLException;
import java.util.List;

public class TransactionService {
    private final TransactionDAO transactionDAO;
    private final BudgetService budgetService;

    public TransactionService(BudgetService budgetService) {
        this.transactionDAO = new TransactionDAO();
        this.budgetService = budgetService;
    }

    public void addTransaction(Transaction transaction, NotificationSender notificationSender) throws SQLException {
        transactionDAO.createTransaction(transaction);

        // Check budget after adding expense transaction
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

    public List<Transaction> getTransactionsByUser(int userId) throws SQLException {
        return transactionDAO.getTransactionsByUser(userId);
    }

    public List<Transaction> getMonthlyTransactions(int userId, int month, int year) throws SQLException {
        return transactionDAO.getTransactionsByUserAndMonth(userId, month, year);
    }

    public void deleteTransaction(int transactionId) throws SQLException {
        transactionDAO.deleteTransaction(transactionId);
    }

    public double calculateMonthlyIncome(int userId, int month, int year) throws SQLException {
        return getMonthlyTransactions(userId, month, year).stream()
            .filter(t -> t.getType().name().equals("INCOME"))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }

    public double calculateMonthlyExpense(int userId, int month, int year) throws SQLException {
        return getMonthlyTransactions(userId, month, year).stream()
            .filter(t -> t.getType().name().equals("EXPENSE"))
            .mapToDouble(Transaction::getAmount)
            .sum();
    }
}
