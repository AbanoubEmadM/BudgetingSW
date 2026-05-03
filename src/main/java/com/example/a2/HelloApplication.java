package com.example.a2;

import com.example.a2.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Application - Personal Budgeting System
 * Demonstrates SOLID principles and Design Patterns:
 * - Singleton: DatabaseManager, AuthenticationManager
 * - Factory: TransactionFactory
 * - Strategy: AlertStrategy (ThresholdAlertStrategy)
 * - Dependency Inversion: NotificationSender interface
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize database
        DatabaseManager.getInstance();

        // Load login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/a2/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 400);
        stage.setTitle("Personal Budgeting System - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        // Clean up database connection on application exit
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}