package com.example.a2.ui;

import com.example.a2.service.AuthenticationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AuthenticationManager authManager = AuthenticationManager.getInstance();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        try {
            if (authManager.login(email, password)) {
                openDashboard();
            } else {
                showError("Invalid email or password");
            }
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        try {
            if (authManager.register(email, password)) {
                showError("Registration successful! Please login.");
                errorLabel.setStyle("-fx-text-fill: green;");
                passwordField.clear();
            } else {
                showError("Email already exists");
            }
        } catch (Exception e) {
            showError( e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void openDashboard() {
        try {
            var url = getClass().getResource("/com/example/a2/fxml/dashboard.fxml");
            if (url == null) {
                showError("Dashboard FXML file not found");
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Budget Dashboard");
        } catch (IOException e) {
            showError("Failed to open dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
