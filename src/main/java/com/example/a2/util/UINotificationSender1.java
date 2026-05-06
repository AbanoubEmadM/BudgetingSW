package com.example.a2.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * {@link NotificationSender} that shows blocking JavaFX {@link Alert} dialogs (warning style by default).
 * Uses standard {@link AlertType#WARNING}; no custom CSS style class is applied on the alert node.
 *
 * @author Abanoub
 * @version 1.0
 * @see NotificationSender
 */
public class UINotificationSender implements NotificationSender {

    /**
     * Displays a modal warning alert with the given title and message.
     *
     * @param title   dialog title
     * @param message body text (header is cleared)
     * @return nothing
     */
    @Override
    public void sendNotification(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
