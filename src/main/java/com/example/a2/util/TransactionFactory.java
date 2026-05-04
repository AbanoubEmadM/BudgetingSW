package com.example.a2.util;

import com.example.a2.model.Transaction;
import com.example.a2.model.TransactionType;

import java.time.LocalDate;

/**
 * Factory Pattern - Creates transactions
 */
public class TransactionFactory {

    public static Transaction createTransaction(int userId, double amount, TransactionType type,
                                               int categoryId, LocalDate date, String description) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategoryId(categoryId);
        transaction.setTransactionDate(date);
        transaction.setDescription(description);
        return transaction;
    }

    public static Transaction createIncomeTransaction(int userId, double amount, int categoryId,
                                                     LocalDate date, String description) {
        return createTransaction(userId, amount, TransactionType.INCOME, categoryId, date, description);
    }

    public static Transaction createExpenseTransaction(int userId, double amount, int categoryId,
                                                      LocalDate date, String description) {
        return createTransaction(userId, amount, TransactionType.EXPENSE, categoryId, date, description);
    }
}
