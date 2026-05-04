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
 * Controller for {@code goals.fxml}: lists goals with {@link ProgressBar} progress, monthly estimate, and CRUD dialogs.
 * Table cells use programmatic formatting; {@link ProgressBar} max width is set in code (no CSS style class).
 *
 * @author Abanoub
 * @version 1.0
 * @see GoalService
 * @see CreateGoalController
 * @see AddContributionController
 */
public class GoalController {

    /** Main goals table bound to {@link #goals}. */
    @FXML private TableView<Goal> goalsTable;
    /** Goal title column (uses {@link Goal#nameProperty()} to avoid JPMS reflection issues). */
    @FXML private TableColumn<Goal, String> nameColumn;
    /** Target amount column. */
    @FXML private TableColumn<Goal, Double> targetColumn;
    /** Saved amount column. */
    @FXML private TableColumn<Goal, Double> savedColumn;
    /** Deadline as ISO string. */
    @FXML private TableColumn<Goal, String> deadlineColumn;
    /** Estimated monthly savings needed. */
    @FXML private TableColumn<Goal, Void> monthlyColumn;
    /** Progress percentage text column. */
    @FXML private TableColumn<Goal, Void> percentColumn;
    /** {@link ProgressBar} column. */
    @FXML private TableColumn<Goal, Void> progressColumn;

    /** Current session. */
    private final AuthenticationManager authManager = AuthenticationManager.getInstance();
    /** Goal operations. */
    private final GoalService goalService = new GoalService();

    /** Observable list with property extractors for row invalidation. */
    private ObservableList<Goal> goals;

    /**
     * [FXML] Wires table columns, sets "%" header in code (FXML cannot start with {@code %}), and loads data.
     *
     * @return nothing
     */
    @FXML
    private void initialize() {
        goals = FXCollections.observableArrayList(goal -> new javafx.beans.Observable[]{
            goal.nameProperty(),
            goal.targetAmountProperty(),
            goal.savedAmountProperty(),
            goal.deadlineProperty()
        });
        goalsTable.setItems(goals);

        percentColumn.setText("%");

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
            /**
             * Renders estimated monthly savings from {@link GoalService#monthlySavingsNeeded(double, double, java.time.LocalDate)}.
             *
             * @param item  unused (void column)
             * @param empty whether the cell is empty
             */
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
            /**
             * Renders {@link Goal#getProgressPercent()} as a whole percent.
             *
             * @param item  unused
             * @param empty row empty flag
             */
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
            /** Progress control bound to saved/target ratio (clamped to 1). */
            private final ProgressBar bar = new ProgressBar();

            {
                bar.setMaxWidth(Double.MAX_VALUE);
            }

            /**
             * Binds {@link ProgressBar#progressProperty()} to goal saved/target observables.
             *
             * @param item  unused
             * @param empty row empty flag
             */
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

    /**
     * Creates a {@link TableCell} that formats {@link Double} amounts as currency.
     *
     * @return cell factory instance
     */
    private TableCell<Goal, Double> currencyCell() {
        return new TableCell<>() {
            /**
             * Formats money with two decimal places.
             *
             * @param amount bound value
             * @param empty  empty flag
             */
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

    /**
     * [FXML] Reloads goals for the logged-in user from {@link GoalService#loadGoalsForUser(int)}.
     *
     * @return nothing
     */
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

    /**
     * [FXML] Opens the create-goal dialog.
     *
     * @return nothing
     */
    @FXML
    private void handleAddGoal() {
        openGoalDialog(null);
    }

    /**
     * [FXML] Opens the edit dialog for the selected goal.
     *
     * @return nothing
     */
    @FXML
    private void handleEditGoal() {
        Goal selected = goalsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Goals", "Select a goal to edit.", Alert.AlertType.INFORMATION);
            return;
        }
        openGoalDialog(selected);
    }

    /**
     * Opens {@code create-goal.fxml} in create or edit mode.
     *
     * @param existing {@code null} to create, otherwise the goal to edit
     * @return nothing
     */
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

    /**
     * [FXML] Deletes the selected goal after confirmation.
     *
     * @return nothing
     */
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

    /**
     * [FXML] Opens {@code add-contribution.fxml} for the selected goal.
     *
     * @return nothing
     */
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

    /**
     * Shows a modal {@link Alert}.
     *
     * @param title   dialog title
     * @param message body text
     * @param type    alert severity
     * @return nothing
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
