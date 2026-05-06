package com.example.a2.util;

import com.example.a2.model.Budget;

/**
 * Strategy interface for deciding when a monthly {@link Budget} should trigger an alert.
 *
 * @author Abanoub
 * @version 1.0
 * @see ThresholdAlertStrategy
 * @see com.example.a2.service.BudgetService
 */
public interface AlertStrategy {

    /**
     * Determines whether an alert should be raised for the given budget snapshot.
     *
     * @param budget budget row including spent and limit amounts
     * @return {@code true} if the user should be notified
     */
    boolean shouldAlert(Budget budget);

    /**
     * Builds the human-readable alert body for the given budget.
     *
     * @param budget budget row used to format percentages and amounts
     * @return message text suitable for {@link NotificationSender}
     */
    String getMessage(Budget budget);
}
