package com.example.a2.model;

/**
 * Monthly spending limit per category for a user, with computed spent and remaining values.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.BudgetDAO
 * @see com.example.a2.service.BudgetService
 */
public class Budget {

    /** Primary key from SQLite. */
    private int id;
    /** Owning user id. */
    private int userId;
    /** Category foreign key. */
    private int categoryId;
    /** Denormalized category name for UI. */
    private String categoryName;
    /** Budget cap amount for the month. */
    private double amount;
    /** Calendar month (1–12). */
    private int month;
    /** Calendar year. */
    private int year;
    /** Total expenses in this category for the month (derived). */
    private double spent;

    /**
     * Constructs an empty budget for DAO mapping.
     */
    public Budget() {}

    /**
     * Constructs a budget row without spent totals (filled by service/DAO).
     *
     * @param userId     owning user
     * @param categoryId category foreign key
     * @param amount     budget limit
     * @param month      month number
     * @param year       year number
     */
    public Budget(int userId, int categoryId, double amount, int month, int year) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }

    /**
     * Returns the budget row id.
     *
     * @return primary key
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the budget row id.
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
     * Returns the category display name.
     *
     * @return joined category label
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Sets the category display name.
     *
     * @param categoryName label for tables
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Returns the budget limit amount.
     *
     * @return cap for the month
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the budget limit amount.
     *
     * @param amount cap for the month
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Returns the calendar month.
     *
     * @return month in range 1–12
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets the calendar month.
     *
     * @param month month in range 1–12
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * Returns the calendar year.
     *
     * @return four-digit year
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the calendar year.
     *
     * @param year four-digit year
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Returns total spent in this category for the month.
     *
     * @return aggregated expense amount
     */
    public double getSpent() {
        return spent;
    }

    /**
     * Sets total spent (typically from {@link com.example.a2.dao.TransactionDAO}).
     *
     * @param spent aggregated expense amount
     */
    public void setSpent(double spent) {
        this.spent = spent;
    }

    /**
     * Computes remaining budget as {@code amount - spent}.
     *
     * @return dollars left before exceeding cap
     */
    public double getRemaining() {
        return amount - spent;
    }

    /**
     * Computes percentage of budget used ({@code spent / amount * 100}).
     *
     * @return usage percentage (undefined if {@code amount == 0} at call site)
     */
    public double getPercentageUsed() {
        return (spent / amount) * 100;
    }
}
