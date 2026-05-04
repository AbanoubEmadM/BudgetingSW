package com.example.a2.ui;

import com.example.a2.model.Category;
import com.example.a2.model.TransactionType;
import com.example.a2.service.AuthenticationManager;
import com.example.a2.service.BudgetService;
import com.example.a2.service.CategoryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class SetBudgetController {

    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private Label errorLabel;

    private final CategoryService categoryService = new CategoryService();
    private final BudgetService budgetService = new BudgetService();
    private final AuthenticationManager authManager = AuthenticationManager.getInstance();

    @FXML
    private void initialize() {
        try {
            // Load expense categories only (budgets are for expenses)
            List<Category> categories = categoryService.getCategoriesByType(TransactionType.EXPENSE);
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            if (!categories.isEmpty()) {
                categoryComboBox.getSelectionModel().selectFirst();
            }

            // Populate months
            for (int i = 1; i <= 12; i++) {
                monthComboBox.getItems().add(
                    java.time.Month.of(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                );
            }

            // Populate years
            LocalDate now = LocalDate.now();
            for (int i = now.getYear() - 1; i <= now.getYear() + 2; i++) {
                yearComboBox.getItems().add(i);
            }

            // Set current month and year
            monthComboBox.getSelectionModel().select(now.getMonthValue() - 1);
            yearComboBox.getSelectionModel().select(Integer.valueOf(now.getYear()));

        } catch (Exception e) {
            showError("Failed to initialize: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        if (categoryComboBox.getValue() == null) {
            showError("Please select a category");
            return;
        }

        if (amountField.getText().trim().isEmpty()) {
            showError("Please enter amount");
            return;
        }

        if (monthComboBox.getValue() == null || yearComboBox.getValue() == null) {
            showError("Please select month and year");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                showError("Amount must be greater than 0");
                return;
            }

            Category category = categoryComboBox.getValue();
            int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;
            int year = yearComboBox.getValue();

            budgetService.setBudget(
                authManager.getCurrentUser().getId(),
                category.getId(),
                amount,
                month,
                year
            );

            handleCancel();

        } catch (NumberFormatException e) {
            showError("Invalid amount format");
        } catch (Exception e) {
            showError("Failed to save budget: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
