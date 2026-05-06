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

/**
 * Controller for {@code add-transaction.fxml}: captures type, category, amount, date, and description.
 * Buttons use inline styles ({@code -fx-background-color}) from FXML; no custom style class names on nodes.
 *
 * @author Abanoub
 * @version 1.0
 * @see DashboardController
 * @see TransactionService
 */
public class AddTransactionController {

    /** Income vs expense selector. */
    @FXML private ComboBox<TransactionType> typeComboBox;
    /** Categories filtered by selected type. */
    @FXML private ComboBox<Category> categoryComboBox;
    /** Decimal amount entry. */
    @FXML private TextField amountField;
    /** Transaction effective date. */
    @FXML private DatePicker datePicker;
    /** Optional memo. */
    @FXML private TextArea descriptionArea;
    /** Validation message label (typically red via FXML or runtime). */
    @FXML private Label errorLabel;

    /** Loads categories. */
    private final CategoryService categoryService = new CategoryService();
    /** Shared with {@link TransactionService} for budget alerts. */
    private final BudgetService budgetService = new BudgetService();
    /** Persists transactions and triggers alerts. */
    private final TransactionService transactionService = new TransactionService(budgetService);
    /** Current user id for new rows. */
    private final AuthenticationManager authManager = AuthenticationManager.getInstance();

    /**
     * [FXML] Initializes combo boxes, default date, and category list for the default type.
     *
     * @return nothing
     */
    @FXML
    private void initialize() {
        typeComboBox.setItems(FXCollections.observableArrayList(TransactionType.values()));
        typeComboBox.getSelectionModel().selectFirst();
        typeComboBox.setOnAction(e -> updateCategories());

        datePicker.setValue(LocalDate.now());

        updateCategories();
    }

    /**
     * Reloads {@link #categoryComboBox} items for the selected {@link TransactionType}.
     *
     * @return nothing
     */
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

    /**
     * [FXML] Validates input, builds a {@link Transaction} via {@link TransactionFactory}, saves, and closes.
     *
     * @return nothing
     */
    @FXML
    private void handleSave() {
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

            Transaction transaction = TransactionFactory.createTransaction(
                authManager.getCurrentUser().getId(),
                amount,
                type,
                category.getId(),
                date,
                description
            );

            transactionService.addTransaction(transaction, new UINotificationSender());

            handleCancel();

        } catch (NumberFormatException e) {
            showError("Invalid amount format");
        } catch (Exception e) {
            showError("Failed to save transaction: " + e.getMessage());
        }
    }

    /**
     * [FXML] Closes the modal dialog without persisting.
     *
     * @return nothing
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) amountField.getScene().getWindow();
        stage.close();
    }

    /**
     * Displays a validation message on {@link #errorLabel}.
     *
     * @param message error text
     * @return nothing
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
