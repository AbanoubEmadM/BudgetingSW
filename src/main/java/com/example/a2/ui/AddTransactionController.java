package com.example.a2.ui;

import com.example.a2.model.Category;
import com.example.a2.model.Transaction;
import com.example.a2.model.TransactionType;
import com.example.a2.service.AuthenticationManager;
import com.example.a2.service.BudgetService;
import com.example.a2.service.CategoryService;
import com.example.a2.service.TransactionService;
import com.example.a2.util.TransactionFactory;
import com.example.a2.util.UINotificationSender;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class AddTransactionController {

    @FXML private ComboBox<TransactionType> typeComboBox;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;

    private final CategoryService categoryService = new CategoryService();
    private final BudgetService budgetService = new BudgetService();
    private final TransactionService transactionService = new TransactionService(budgetService);
    private final AuthenticationManager authManager = AuthenticationManager.getInstance();

    @FXML
    private void initialize() {
        // Setup type combo box
        typeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        typeComboBox.getSelectionModel().selectFirst();
        typeComboBox.setOnAction(e -> updateCategories());

        // Set default date to today
        datePicker.setValue(LocalDate.now());

        // Load initial categories
        updateCategories();
    }

    private void updateCategories() {
        try {
            TransactionType selectedType = typeComboBox.getValue();
            if (selectedType != null) {
                List<Category> categories = categoryService.getCategoriesByType(selectedType);
                categoryComboBox.setItems(FXCollections.observableArrayList(categories));
                if (!categories.isEmpty()) {
                    categoryComboBox.getSelectionModel().selectFirst();
                }
            }
        } catch (Exception e) {
            showError("Failed to load categories: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        if (typeComboBox.getValue() == null) {
            showError("Please select transaction type");
            return;
        }

        if (categoryComboBox.getValue() == null) {
            showError("Please select a category");
            return;
        }

        if (amountField.getText().trim().isEmpty()) {
            showError("Please enter amount");
            return;
        }

        if (datePicker.getValue() == null) {
            showError("Please select a date");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                showError("Amount must be greater than 0");
                return;
            }

            TransactionType type = typeComboBox.getValue();
            Category category = categoryComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String description = descriptionArea.getText().trim();

            // Create transaction using factory
            Transaction transaction = TransactionFactory.createTransaction(
                authManager.getCurrentUser().getId(),
                amount,
                type,
                category.getId(),
                date,
                description
            );

            // Save transaction
            transactionService.addTransaction(transaction, new UINotificationSender());

            // Close window
            handleCancel();

        } catch (NumberFormatException e) {
            showError("Invalid amount format");
        } catch (Exception e) {
            showError("Failed to save transaction: " + e.getMessage());
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
