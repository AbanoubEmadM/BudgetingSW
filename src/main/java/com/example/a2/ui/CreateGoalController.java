package com.example.a2.ui;

import com.example.a2.model.Goal;
import com.example.a2.service.AuthenticationManager;
import com.example.a2.service.GoalService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controller for {@code create-goal.fxml}: modal create/edit form for {@link Goal} fields.
 * Buttons use inline {@code -fx-background-color} from FXML (no custom style class names).
 *
 * @author Abanoub
 * @version 1.0
 * @see GoalController
 * @see GoalService
 */
public class CreateGoalController {

    /** Dialog title ("Create goal" / "Edit goal"). */
    @FXML private Label titleLabel;
    /** Goal display name. */
    @FXML private TextField nameField;
    /** Target amount text. */
    @FXML private TextField targetField;
    /** Goal deadline. */
    @FXML private DatePicker deadlinePicker;
    /** Optional starting balance (create mode only). */
    @FXML private TextField initialField;
    /** Validation message label. */
    @FXML private Label errorLabel;

    /** Session user id for saves. */
    private final AuthenticationManager authManager = AuthenticationManager.getInstance();
    /** Goal business rules. */
    private final GoalService goalService = new GoalService();

    /** Non-null when editing an existing goal. */
    private Goal editingGoal;

    /**
     * [FXML] Clears error state on load.
     *
     * @return nothing
     */
    @FXML
    private void initialize() {
        clearError();
    }

    /**
     * Prepares the form for creating a new goal (shows initial saved field).
     *
     * @return nothing
     */
    public void initForCreate() {
        editingGoal = null;
        titleLabel.setText("Create goal");
        nameField.clear();
        targetField.clear();
        deadlinePicker.setValue(null);
        initialField.clear();
        initialField.setVisible(true);
        initialField.setManaged(true);
        deadlinePicker.setDisable(false);
    }

    /**
     * Prepares the form for editing an existing goal (hides initial saved field).
     *
     * @param goal goal to edit (same instance updated on save)
     * @return nothing
     */
    public void initForEdit(Goal goal) {
        editingGoal = goal;
        titleLabel.setText("Edit goal");
        nameField.setText(goal.getName());
        targetField.setText(String.valueOf(goal.getTargetAmount()));
        deadlinePicker.setValue(goal.getDeadline());
        initialField.clear();
        initialField.setVisible(false);
        initialField.setManaged(false);
    }

    /**
     * [FXML] Validates and calls {@link GoalService#createGoal} or {@link GoalService#updateGoal}.
     *
     * @return nothing
     */
    @FXML
    private void handleSave() {
        clearError();
        try {
            String name = nameField.getText();
            GoalService.validateName(name);

            if (deadlinePicker.getValue() == null) {
                showError("Please choose a deadline.");
                return;
            }

            double target = parseRequiredMoney(targetField.getText(), "Target amount");
            GoalService.validateTargetAmount(target);

            int userId = authManager.getCurrentUser().getId();

            if (editingGoal == null) {
                GoalService.validateDeadlineFuture(deadlinePicker.getValue());
                Double initial = parseOptionalMoney(initialField.getText());
                goalService.createGoal(userId, name, target, deadlinePicker.getValue(), initial);
            } else {
                if (deadlinePicker.getValue().isBefore(LocalDate.now())) {
                    showError("Deadline cannot be in the past.");
                    return;
                }
                goalService.updateGoal(editingGoal, name, target, deadlinePicker.getValue());
            }
            close();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError(ex.getMessage() != null ? ex.getMessage() : "Could not save goal.");
            ex.printStackTrace();
        }
    }

    /**
     * [FXML] Closes the dialog without saving.
     *
     * @return nothing
     */
    @FXML
    private void handleCancel() {
        close();
    }

    /**
     * Closes the hosting {@link Stage}.
     *
     * @return nothing
     */
    private void close() {
        Stage s = (Stage) nameField.getScene().getWindow();
        s.close();
    }

    /**
     * Parses a required non-empty decimal field.
     *
     * @param raw   user text
     * @param label field label for error messages
     * @return parsed amount
     * @throws IllegalArgumentException if blank or not a number
     */
    private double parseRequiredMoney(String raw, String label) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    /**
     * Parses an optional decimal field (blank means absent).
     *
     * @param raw user text
     * @return parsed value or {@code null} if blank
     * @throws IllegalArgumentException if non-blank but invalid
     */
    private Double parseOptionalMoney(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Initial saved must be a valid number.");
        }
    }

    /**
     * Shows {@link #errorLabel} with the given message.
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
     * Hides and clears {@link #errorLabel}.
     *
     * @return nothing
     */
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
