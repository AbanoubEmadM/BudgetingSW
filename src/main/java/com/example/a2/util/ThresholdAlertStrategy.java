package com.example.a2.util;

import com.example.a2.model.Budget;

/**
 * Strategy Pattern - Alert when budget exceeds threshold percentage
 */
public class ThresholdAlertStrategy implements AlertStrategy {
    private final double thresholdPercentage;

    public ThresholdAlertStrategy(double thresholdPercentage) {
        this.thresholdPercentage = thresholdPercentage;
    }

    @Override
    public boolean shouldAlert(Budget budget) {
        return budget.getPercentageUsed() >= thresholdPercentage;
    }

    @Override
    public String getMessage(Budget budget) {
        return String.format(
            "Budget Alert: %s\n\nYou have spent %.2f%% (%.2f / %.2f) of your budget.\nRemaining: %.2f",
            budget.getCategoryName(),
            budget.getPercentageUsed(),
            budget.getSpent(),
            budget.getAmount(),
            budget.getRemaining()
        );
    }
}
