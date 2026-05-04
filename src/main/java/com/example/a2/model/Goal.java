package com.example.a2.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Financial savings goal with JavaFX {@link javafx.beans.property} wrappers for reactive UI binding
 * (e.g. {@link javafx.scene.control.TableView}, {@link javafx.scene.control.ProgressBar}).
 *
 * @author Abanoub
 * @version 1.0
 * @see com.example.a2.dao.GoalDAO
 * @see com.example.a2.service.GoalService
 * @see com.example.a2.ui.GoalController
 */
public class Goal {

    /** Bound property: persistent goal id (0 before insert). */
    private final IntegerProperty id = new SimpleIntegerProperty(0);
    /** Bound property: owning user id. */
    private final IntegerProperty userId = new SimpleIntegerProperty(0);
    /** Bound property: user-visible goal title. */
    private final StringProperty name = new SimpleStringProperty("");
    /** Bound property: savings target amount. */
    private final DoubleProperty targetAmount = new SimpleDoubleProperty(0);
    /** Bound property: amount saved toward the goal. */
    private final DoubleProperty savedAmount = new SimpleDoubleProperty(0);
    /** Bound property: target completion date. */
    private final ObjectProperty<LocalDate> deadline = new SimpleObjectProperty<>();
    /** Bound property: row creation time from persistence. */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    /**
     * Constructs an empty goal for DAO mapping or forms.
     */
    public Goal() {
    }

    /**
     * Constructs a goal with core business fields (id may be assigned by DAO).
     *
     * @param userId        owning user
     * @param name          goal title
     * @param targetAmount  savings target
     * @param savedAmount   current progress
     * @param deadline      target date
     */
    public Goal(int userId, String name, double targetAmount, double savedAmount, LocalDate deadline) {
        setUserId(userId);
        setName(name);
        setTargetAmount(targetAmount);
        setSavedAmount(savedAmount);
        setDeadline(deadline);
    }

    /**
     * Returns the goal id (JavaBean accessor for {@link #idProperty}).
     *
     * @return current id value
     */
    public int getId() {
        return id.get();
    }

    /**
     * Sets the goal id (updates {@link #idProperty}).
     *
     * @param id new id value
     */
    public void setId(int id) {
        this.id.set(id);
    }

    /**
     * JavaFX property for the goal id; bind for reactive UI.
     *
     * @return {@link IntegerProperty} backing {@code id}
     */
    public IntegerProperty idProperty() {
        return id;
    }

    /**
     * Returns the owning user id.
     *
     * @return user id
     */
    public int getUserId() {
        return userId.get();
    }

    /**
     * Sets the owning user id.
     *
     * @param userId user id
     */
    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    /**
     * JavaFX property for the owning user id.
     *
     * @return {@link IntegerProperty} backing {@code userId}
     */
    public IntegerProperty userIdProperty() {
        return userId;
    }

    /**
     * Returns the goal name.
     *
     * @return title string
     */
    public String getName() {
        return name.get();
    }

    /**
     * Sets the goal name.
     *
     * @param name title string
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * JavaFX property for the goal name; use with {@code TableColumn} cell value factories.
     *
     * @return {@link StringProperty} backing {@code name}
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * Returns the target savings amount.
     *
     * @return target value
     */
    public double getTargetAmount() {
        return targetAmount.get();
    }

    /**
     * Sets the target savings amount.
     *
     * @param targetAmount target value
     */
    public void setTargetAmount(double targetAmount) {
        this.targetAmount.set(targetAmount);
    }

    /**
     * JavaFX property for the target amount.
     *
     * @return {@link DoubleProperty} backing {@code targetAmount}
     */
    public DoubleProperty targetAmountProperty() {
        return targetAmount;
    }

    /**
     * Returns the amount saved so far.
     *
     * @return saved progress
     */
    public double getSavedAmount() {
        return savedAmount.get();
    }

    /**
     * Sets the amount saved so far.
     *
     * @param savedAmount progress value
     */
    public void setSavedAmount(double savedAmount) {
        this.savedAmount.set(savedAmount);
    }

    /**
     * JavaFX property for saved amount (bind to {@link javafx.scene.control.ProgressBar} numerator).
     *
     * @return {@link DoubleProperty} backing {@code savedAmount}
     */
    public DoubleProperty savedAmountProperty() {
        return savedAmount;
    }

    /**
     * Returns the goal deadline date.
     *
     * @return deadline, may be {@code null}
     */
    public LocalDate getDeadline() {
        return deadline.get();
    }

    /**
     * Sets the goal deadline date.
     *
     * @param deadline target date
     */
    public void setDeadline(LocalDate deadline) {
        this.deadline.set(deadline);
    }

    /**
     * JavaFX property for the deadline.
     *
     * @return {@link ObjectProperty} of {@link LocalDate}
     */
    public ObjectProperty<LocalDate> deadlineProperty() {
        return deadline;
    }

    /**
     * Returns the creation timestamp from persistence.
     *
     * @return created-at value, may be {@code null}
     */
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt persistence timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    /**
     * JavaFX property for creation time.
     *
     * @return {@link ObjectProperty} of {@link LocalDateTime}
     */
    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    /**
     * Computes progress ratio in {@code [0, 1]} for {@link javafx.scene.control.ProgressBar#setProgress(double)}.
     *
     * @return {@code min(1, saved/target)} or {@code 0} if target is non-positive
     */
    public double getProgressRatio() {
        double t = getTargetAmount();
        if (t <= 0) {
            return 0;
        }
        return Math.min(1.0, getSavedAmount() / t);
    }

    /**
     * Computes progress as a percentage (may exceed 100 if saved exceeds target).
     *
     * @return {@code saved/target*100} or {@code 0} if target is non-positive
     */
    public double getProgressPercent() {
        double t = getTargetAmount();
        if (t <= 0) {
            return 0;
        }
        return (getSavedAmount() / t) * 100.0;
    }
}
