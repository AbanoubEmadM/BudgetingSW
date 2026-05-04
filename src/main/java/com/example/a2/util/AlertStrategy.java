package com.example.a2.util;

import com.example.a2.model.Budget;

/**
 * Strategy Pattern - Alert strategy interface
 */
public interface AlertStrategy {
    boolean shouldAlert(Budget budget);
    String getMessage(Budget budget);
}
