package com.example.a2.util;

import com.example.a2.model.Budget;

/**
 * {@link AlertStrategy} implementation that fires when {@link Budget#getPercentageUsed()} crosses a threshold.
 *
 * @author Abanoub
 * @version 1.0
 * @see AlertStrategy
 * @see com.example.a2.service.BudgetService
 */
public class ThresholdAlertStrategy implements AlertStrategy {

    /** Percentage of budget used at or above which an alert is raised (e.g. 80.0). */
    private final double thresholdPercentage;

    /**
     * Creates a strategy with the given usage threshold.
     *
     * @param thresholdPercentage minimum percentage (0–100 style) to trigger alerts
     */
    public ThresholdAlertStrategy(double thresholdPercentage) {
        this.thresholdPercentage = thresholdPercentage;
    }

    /**
     * Returns {@code true} when spent percentage meets or exceeds the configured threshold.
     *
     * @param budget budget snapshot including spent and limit
     * @return whether to notify the user
     */
    @Override
    public boolean shouldAlert(Budget budget) {
        return budget.getPercentageUsed() >= thresholdPercentage;
    }

    /**
     * Formats a multi-line alert describing category usage and remaining funds.
     *
     * @param budget budget used for labels and amounts
     * @return message body for {@link NotificationSender}
     */
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
