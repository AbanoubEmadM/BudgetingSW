package com.example.a2.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single monetary movement (income or expense) for a user.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.TransactionDAO
 * @see com.example.a2.service.TransactionService
 */
public class Transaction {

    /** Primary key from SQLite. */
    private int id;
    /** Owning user id. */
    private int userId;
    /** Positive amount in account currency. */
    private double amount;
    /** Income vs expense classification. */
    private TransactionType type;
    /** Foreign key to {@code categories}. */
    private int categoryId;
    /** Denormalized category label from join queries. */
    private String categoryName;
    /** Calendar date of the transaction. */
    private LocalDate transactionDate;
    /** Optional user note. */
    private String description;
    /** Insert time from the database. */
    private LocalDateTime createdAt;

    /**
     * Constructs an empty transaction for DAO population.
     */
    public Transaction() {}

    /**
     * Constructs a transaction without database-generated fields.
     *
     * @param userId           owning user
     * @param amount           positive amount
     * @param type             income or expense
     * @param categoryId       category foreign key
     * @param transactionDate  effective date
     * @param description      optional note
     */
    public Transaction(int userId, double amount, TransactionType type, int categoryId,
                      LocalDate transactionDate, String description) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.transactionDate = transactionDate;
        this.description = description;
    }

    /**
     * Returns the transaction id.
     *
     * @return primary key
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the transaction id.
     *
     * @param id primary key
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the owning user id.
     *
     * @return user foreign key
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the owning user id.
     *
     * @param userId user foreign key
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Returns the transaction amount.
     *
     * @return positive monetary value
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the transaction amount.
     *
     * @param amount positive monetary value
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Returns income vs expense type.
     *
     * @return {@link TransactionType}
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Sets income vs expense type.
     *
     * @param type transaction classification
     */
    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     * Returns the category id.
     *
     * @return category foreign key
     */
    public int getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the category id.
     *
     * @param categoryId category foreign key
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Returns the joined category display name.
     *
     * @return category label for UI tables
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Sets the joined category display name.
     *
     * @param categoryName label from SQL join
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Returns the transaction date.
     *
     * @return calendar date of the movement
     */
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets the transaction date.
     *
     * @param transactionDate calendar date
     */
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * Returns the optional description.
     *
     * @return user note, may be {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the optional description.
     *
     * @param description user note
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the row creation timestamp.
     *
     * @return database {@code created_at}, may be {@code null}
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the row creation timestamp.
     *
     * @param createdAt persistence timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
