package com.example.a2.model;

/**
 * Enumerates whether a {@link Transaction} or {@link Category} represents income or expense.
 *
 * @author Abanoub
 * @version 1.0
 * @see Transaction
 * @see Category
 */
public enum TransactionType {
    /** Money received (e.g. salary). */
    INCOME,
    /** Money spent (e.g. food). */
    EXPENSE
}
