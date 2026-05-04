package com.example.a2.util;

/**
 * Dependency Inversion - NotificationSender interface
 */
public interface NotificationSender {
    void sendNotification(String title, String message);
}
