package com.example.a2.ui;

import com.example.a2.model.Goal;
import com.example.a2.service.AuthenticationManager;
import com.example.a2.service.GoalService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Goals tab: list, progress, contributions, create / edit / delete.
 */
public class GoalController {

    @FXML private TableView<Goal> goalsTable;
    @FXML private TableColumn<Goal, String> nameColumn;
    @FXML private TableColumn<Goal, Double> targetColumn;
    @FXML private TableColumn<Goal, Double> savedColumn;
    @FXML private TableColumn<Goal, String> deadlineColumn;
    @FXML private TableColumn<Goal, Void> monthlyColumn;
    @FXML private TableColumn<Goal, Void> percentColumn;
    @FXML private TableColumn<Goal, Void> progressColumn;

    private final AuthenticationManager authManager = AuthenticationManager.getInstance();
    private final GoalService goalService = new GoalService();

    private ObservableList<Goal> goals;

    @FXML
    private void initialize() {
        goals = FXCollections.observableArrayList(goal -> new javafx.beans.Observable[]{
            goal.nameProperty(),
            goal.targetAmountProperty(),
            goal.savedAmountProperty(),
            goal.deadlineProperty()
        });
        goalsTable.setItems(goals);

        // Cannot set text="%" in FXML: leading % is treated as a resource-bundle key.
        percentColumn.setText("%");

        // Avoid PropertyValueFactory: JPMS does not export model to javafx.controls, so reflection fails.
        nameColumn.setCellValueFactory(cd -> cd.getValue().nameProperty());

        targetColumn.setCellValueFactory(cd -> cd.getValue().targetAmountProperty().asObject());
        targetColumn.setCellFactory(col -> currencyCell());

        savedColumn.setCellValueFactory(cd -> cd.getValue().savedAmountProperty().asObject());
        savedColumn.setCellFactory(col -> currencyCell());

        deadlineColumn.setCellValueFactory(cd ->
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cd.getValue().getDeadline() != null ? cd.getValue().getDeadline().toString() : "",
                cd.getValue().deadlineProperty()));

        monthlyColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Goal g = getTableRow().getItem();
                double m = GoalService.monthlySavingsNeeded(g.getSavedAmount(), g.getTargetAmount(), g.getDeadline());
                setText(String.format("$%.2f", m));
            }
        });

        percentColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Goal g = getTableRow().getItem();
                setText(String.format("%.0f%%", g.getProgressPercent()));
            }
        });

        progressColumn.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar();

            {
                bar.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    bar.progressProperty().unbind();
                    setGraphic(null);
                    return;
                }
                Goal g = getTableRow().getItem();
                bar.progressProperty().unbind();
                bar.progressProperty().bind(Bindings.createDoubleBinding(
                    () -> Math.min(1.0, g.getTargetAmount() > 0 ? g.getSavedAmount() / g.getTargetAmount() : 0),
                    g.savedAmountProperty(), g.targetAmountProperty()));
                setGraphic(bar);
            }
        });

        handleRefresh();
    }

    private TableCell<Goal, Double> currencyCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        };
    }

    @FXML
    private void handleRefresh() {
        try {
            int userId = authManager.getCurrentUser().getId();
            List<Goal> list = goalService.loadGoalsForUser(userId);
            goals.setAll(list);
        } catch (Exception e) {
            showAlert("Error", "Failed to load goals: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddGoal() {
        openGoalDialog(null);
    }

    @FXML
    private void handleEditGoal() {
        Goal selected = goalsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Goals", "Select a goal to edit.", Alert.AlertType.INFORMATION);
            return;
        }
        openGoalDialog(selected);
    }

    private void openGoalDialog(Goal existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/a2/fxml/create-goal.fxml"));
            Parent root = loader.load();
            CreateGoalController ctrl = loader.getController();
            if (existing == null) {
                ctrl.initForCreate();
            } else {
                ctrl.initForEdit(existing);
            }

            Stage stage = new Stage();
            stage.setTitle(existing == null ? "Add Goal" : "Edit Goal");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            handleRefresh();
        } catch (IOException e) {
            showAlert("Error", "Could not open goal form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteGoal() {
        Goal selected = goalsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Goals", "Select a goal to delete.", Alert.AlertType.INFORMATION);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete goal");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete \"" + selected.getName() + "\"? This cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    goalService.deleteGoal(selected.getId(), authManager.getCurrentUser().getId());
                    handleRefresh();
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete goal: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleAddContribution() {
        Goal selected = goalsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Goals", "Select a goal first.", Alert.AlertType.INFORMATION);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/a2/fxml/add-contribution.fxml"));
            Parent root = loader.load();
            AddContributionController ctrl = loader.getController();
            ctrl.init(selected, authManager.getCurrentUser().getId(), goalService);

            Stage stage = new Stage();
            stage.setTitle("Add contribution");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            handleRefresh();
        } catch (IOException e) {
            showAlert("Error", "Could not open contribution dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
