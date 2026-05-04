package com.example.a2.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Concrete implementation of NotificationSender for UI alerts
 */
public class UINotificationSender implements NotificationSender {

    @Override
    public void sendNotification(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
