package com.example.a2.ui;

import com.example.a2.model.Goal;
import com.example.a2.service.AuthenticationManager;
import com.example.a2.service.GoalService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Modal form to create or edit a {@link Goal}.
 */
public class CreateGoalController {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField targetField;
    @FXML private DatePicker deadlinePicker;
    @FXML private TextField initialField;
    @FXML private Label errorLabel;
    @FXML private GridPane gridPane;

    private final AuthenticationManager authManager = AuthenticationManager.getInstance();
    private final GoalService goalService = new GoalService();

    private Goal editingGoal;

    @FXML
    private void initialize() {
        clearError();
    }

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

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage s = (Stage) nameField.getScene().getWindow();
        s.close();
    }

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
