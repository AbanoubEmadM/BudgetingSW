package com.example.a2.util;

/**
 * Abstraction for delivering user-visible notifications (dependency inversion).
 * Implemented by {@link UINotificationSender} for JavaFX {@link javafx.scene.control.Alert}s.
 *
 * @author Abanoub
 * @version 1.0
 * @see UINotificationSender
 */
public interface NotificationSender {

    /**
     * Displays or routes a titled notification message to the user.
     *
     * @param title   short title for the notification dialog
     * @param message body text shown to the user
     * @return nothing
     */
    void sendNotification(String title, String message);
}
