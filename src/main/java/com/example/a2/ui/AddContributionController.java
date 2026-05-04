package com.example.a2.ui;

import com.example.a2.model.Goal;
import com.example.a2.model.Transaction;
import com.example.a2.service.GoalService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

/**
 * Add manual savings or link an income {@link Transaction} to a {@link Goal}.
 */
public class AddContributionController {

    @FXML private Label goalTitleLabel;
    @FXML private TabPane modeTabs;
    @FXML private TextField manualAmountField;
    @FXML private ComboBox<Transaction> transactionComboBox;
    @FXML private Label errorLabel;

    private Goal goal;
    private int userId;
    private GoalService goalService;

    @FXML
    private void initialize() {
        clearError();
        transactionComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Transaction t) {
                if (t == null) {
                    return "";
                }
                return String.format("#%d — %s — $%.2f (%s)",
                    t.getId(), t.getCategoryName(), t.getAmount(), t.getTransactionDate());
            }

            @Override
            public Transaction fromString(String string) {
                return null;
            }
        });
    }

    public void init(Goal goal, int userId, GoalService goalService) {
        this.goal = goal;
        this.userId = userId;
        this.goalService = goalService;
        goalTitleLabel.setText("Goal: " + goal.getName());
        manualAmountField.clear();
        try {
            List<Transaction> linkable = goalService.listLinkableIncomeTransactions(userId);
            transactionComboBox.setItems(FXCollections.observableArrayList(linkable));
            if (!linkable.isEmpty()) {
                transactionComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showError("Could not load transactions: " + e.getMessage());
        }
    }

    @FXML
    private void handleApply() {
        clearError();
        try {
            int tab = modeTabs.getSelectionModel().getSelectedIndex();
            if (tab == 0) {
                String raw = manualAmountField.getText();
                if (raw == null || raw.trim().isEmpty()) {
                    showError("Enter an amount.");
                    return;
                }
                double amount = Double.parseDouble(raw.trim());
                goalService.addManualContribution(goal.getId(), userId, amount);
            } else {
                Transaction tx = transactionComboBox.getSelectionModel().getSelectedItem();
                if (tx == null) {
                    showError("Select an income transaction.");
                    return;
                }
                goalService.addContributionFromTransaction(goal.getId(), userId, tx.getId());
            }
            close();
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.");
        } catch (Exception e) {
            showError(e.getMessage() != null ? e.getMessage() : "Could not apply contribution.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage s = (Stage) manualAmountField.getScene().getWindow();
        s.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
