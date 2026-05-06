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
 * Controller for {@code add-contribution.fxml}: manual amount or linking an income {@link Transaction} to a {@link Goal}.
 * {@link TabPane} uses standard tab chrome from FXML (no custom CSS style class names declared in code).
 *
 * @author Abanoub
 * @version 1.0
 * @see GoalController
 * @see GoalService
 */
public class AddContributionController {

    /** Header showing which goal is receiving the contribution. */
    @FXML private Label goalTitleLabel;
    /** Switches between manual amount and transaction link tabs. */
    @FXML private TabPane modeTabs;
    /** Manual contribution amount. */
    @FXML private TextField manualAmountField;
    /** Income transactions not yet linked to a goal. */
    @FXML private ComboBox<Transaction> transactionComboBox;
    /** Validation message label. */
    @FXML private Label errorLabel;

    /** Target goal for this dialog. */
    private Goal goal;
    /** Owning user id (authorization). */
    private int userId;
    /** Goal business operations. */
    private GoalService goalService;

    /**
     * [FXML] Configures the transaction combo {@link StringConverter} for readable rows.
     *
     * @return nothing
     */
    @FXML
    private void initialize() {
        clearError();
        transactionComboBox.setConverter(new StringConverter<>() {
            /**
             * Renders a transaction as id, category, amount, and date.
             *
             * @param t transaction or {@code null}
             * @return display string
             */
            @Override
            public String toString(Transaction t) {
                if (t == null) {
                    return "";
                }
                return String.format("#%d — %s — $%.2f (%s)",
                    t.getId(), t.getCategoryName(), t.getAmount(), t.getTransactionDate());
            }

            /**
             * Not used for read-only combo display.
             *
             * @param string ignored
             * @return always {@code null}
             */
            @Override
            public Transaction fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Binds state for the dialog after FXML load (goal, user, service) and loads linkable transactions.
     *
     * @param goal         selected goal
     * @param userId       current user id
     * @param goalService  service instance from parent
     * @return nothing
     */
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

    /**
     * [FXML] Applies either a manual contribution or a linked transaction based on the selected tab.
     *
     * @return nothing
     */
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

    /**
     * [FXML] Closes the dialog without applying changes.
     *
     * @return nothing
     */
    @FXML
    private void handleCancel() {
        close();
    }

    /**
     * Closes the modal {@link Stage}.
     *
     * @return nothing
     */
    private void close() {
        Stage s = (Stage) manualAmountField.getScene().getWindow();
        s.close();
    }

    /**
     * Shows {@link #errorLabel}.
     *
     * @param message error text
     * @return nothing
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Clears {@link #errorLabel}.
     *
     * @return nothing
     */
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
