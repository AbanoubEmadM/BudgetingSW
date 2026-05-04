package com.example.a2.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Financial savings goal with JavaFX properties for reactive table / progress UI.
 */
public class Goal {

    private final IntegerProperty id = new SimpleIntegerProperty(0);
    private final IntegerProperty userId = new SimpleIntegerProperty(0);
    private final StringProperty name = new SimpleStringProperty("");
    private final DoubleProperty targetAmount = new SimpleDoubleProperty(0);
    private final DoubleProperty savedAmount = new SimpleDoubleProperty(0);
    private final ObjectProperty<LocalDate> deadline = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    public Goal() {
    }

    public Goal(int userId, String name, double targetAmount, double savedAmount, LocalDate deadline) {
        setUserId(userId);
        setName(name);
        setTargetAmount(targetAmount);
        setSavedAmount(savedAmount);
        setDeadline(deadline);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public int getUserId() {
        return userId.get();
    }

    public void setUserId(int userId) {
        this.userId.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userId;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public double getTargetAmount() {
        return targetAmount.get();
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount.set(targetAmount);
    }

    public DoubleProperty targetAmountProperty() {
        return targetAmount;
    }

    public double getSavedAmount() {
        return savedAmount.get();
    }

    public void setSavedAmount(double savedAmount) {
        this.savedAmount.set(savedAmount);
    }

    public DoubleProperty savedAmountProperty() {
        return savedAmount;
    }

    public LocalDate getDeadline() {
        return deadline.get();
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline.set(deadline);
    }

    public ObjectProperty<LocalDate> deadlineProperty() {
        return deadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    /** Progress ratio in [0, 1] for {@link javafx.scene.control.ProgressBar}. */
    public double getProgressRatio() {
        double t = getTargetAmount();
        if (t <= 0) {
            return 0;
        }
        return Math.min(1.0, getSavedAmount() / t);
    }

    /** Progress percentage in [0, 100] (can exceed 100 if saved &gt; target). */
    public double getProgressPercent() {
        double t = getTargetAmount();
        if (t <= 0) {
            return 0;
        }
        return (getSavedAmount() / t) * 100.0;
    }
}
