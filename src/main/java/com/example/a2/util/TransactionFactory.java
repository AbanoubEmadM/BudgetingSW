package com.example.a2.util;

import com.example.a2.model.Transaction;
import com.example.a2.model.TransactionType;

import java.time.LocalDate;

/**
 * Factory for constructing {@link Transaction} instances used by the UI layer (factory pattern).
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.ui.AddTransactionController
 * @see Transaction
 */
public class TransactionFactory {

    /**
     * Creates a transaction with all required fields (no database id).
     *
     * @param userId      owning user id
     * @param amount      positive amount
     * @param type        income or expense
     * @param categoryId  category foreign key
     * @param date        transaction date
     * @param description optional note
     * @return new {@link Transaction} ready for DAO insert
     */
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

    /**
     * Convenience wrapper for {@link TransactionType#INCOME}.
     *
     * @param userId      owning user id
     * @param amount      positive amount
     * @param categoryId  income category id
     * @param date        transaction date
     * @param description optional note
     * @return income transaction
     */
    public static Transaction createIncomeTransaction(int userId, double amount, int categoryId,
                                                     LocalDate date, String description) {
        return createTransaction(userId, amount, TransactionType.INCOME, categoryId, date, description);
    }

    /**
     * Convenience wrapper for {@link TransactionType#EXPENSE}.
     *
     * @param userId      owning user id
     * @param amount      positive amount
     * @param categoryId  expense category id
     * @param date        transaction date
     * @param description optional note
     * @return expense transaction
     */
    public static Transaction createExpenseTransaction(int userId, double amount, int categoryId,
                                                      LocalDate date, String description) {
        return createTransaction(userId, amount, TransactionType.EXPENSE, categoryId, date, description);
    }
}
