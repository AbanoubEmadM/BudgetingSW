package com.example.a2.model;

/**
 * Budget or transaction category (income or expense) loaded from {@code categories}.
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.CategoryDAO
 * @see TransactionType
 */
public class Category {

    /** Primary key from SQLite. */
    private int id;
    /** Display name shown in combo boxes and tables. */
    private String name;
    /** Whether this category applies to income or expense flows. */
    private TransactionType type;

    /**
     * Constructs an empty category for DAO mapping.
     *
     * @return nothing
     */
    public Category() {}

    /**
     * Constructs a fully populated category.
     *
     * @param id   primary key
     * @param name display name
     * @param type income or expense classification
     */
    public Category(int id, String name, TransactionType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    /**
     * Returns the category id.
     *
     * @return primary key
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the category id.
     *
     * @param id primary key
     * @return nothing
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the category display name.
     *
     * @return name string
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the category display name.
     *
     * @param name new name
     * @return nothing
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the income/expense classification.
     *
     * @return {@link TransactionType} value
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Sets the income/expense classification.
     *
     * @param type income or expense
     * @return nothing
     */
    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     * Returns the display name for {@link javafx.scene.control.ComboBox} labels.
     *
     * @return {@link #getName()}
     */
    @Override
    public String toString() {
        return name;
    }
}
