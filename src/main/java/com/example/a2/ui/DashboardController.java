package com.example.a2.ui;

import com.example.a2.model.Budget;
import com.example.a2.model.Transaction;
import com.example.a2.service.AuthenticationManager;
import com.example.a2.service.BudgetService;
import com.example.a2.service.TransactionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label incomeLabel;
    @FXML private Label expenseLabel;
    @FXML private Label balanceLabel;

    // Transactions Table
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> categoryColumn;
    @FXML private TableColumn<Transaction, Double> amountColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;

    // Budgets Table
    @FXML private TableView<Budget> budgetsTable;
    @FXML private TableColumn<Budget, String> budgetCategoryColumn;
    @FXML private TableColumn<Budget, Double> budgetAmountColumn;
    @FXML private TableColumn<Budget, Double> spentColumn;
    @FXML private TableColumn<Budget, Double> remainingColumn;
    @FXML private TableColumn<Budget, Double> percentageColumn;

    // Reports
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private TextArea reportTextArea;

    private final AuthenticationManager authManager = AuthenticationManager.getInstance();
    private final BudgetService budgetService = new BudgetService();
    private final TransactionService transactionService = new TransactionService(budgetService);

    private int currentMonth;
    private int currentYear;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + authManager.getCurrentUser().getEmail());

        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue();
        currentYear = now.getYear();

        setupTransactionsTable();
        setupBudgetsTable();
        setupReportsControls();

        refreshData();
    }

    private void setupTransactionsTable() {
        dateColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTransactionDate().toString()));
        typeColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().toString()));
        categoryColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategoryName()));
        amountColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
        descriptionColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

        amountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
    }

    private void setupBudgetsTable() {
        budgetCategoryColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategoryName()));
        budgetAmountColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getAmount()));
        spentColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getSpent()));
        remainingColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRemaining()));
        percentageColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getPercentageUsed()));

        // Format currency columns
        budgetAmountColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("$%.2f", amount));
            }
        });

        spentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("$%.2f", amount));
            }
        });

        remainingColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("$%.2f", amount));
            }
        });

        percentageColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f%%", percentage));
                    if (percentage >= 80) {
                        setStyle("-fx-background-color: #ffcccb;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupReportsControls() {
        // Populate months
        ObservableList<String> months = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            months.add(java.time.Month.of(i).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }
        monthComboBox.setItems(months);
        monthComboBox.getSelectionModel().select(currentMonth - 1);

        // Populate years
        ObservableList<Integer> years = FXCollections.observableArrayList();
        for (int i = currentYear - 5; i <= currentYear + 1; i++) {
            years.add(i);
        }
        yearComboBox.setItems(years);
        yearComboBox.getSelectionModel().select(Integer.valueOf(currentYear));
    }

    @FXML
    private void refreshData() {
        try {
            int userId = authManager.getCurrentUser().getId();

            // Load transactions
            List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
            System.out.println("Loaded " + transactions.size() + " transactions for user " + userId);
            transactionsTable.setItems(FXCollections.observableArrayList(transactions));

            // Load budgets
            List<Budget> budgets = budgetService.getMonthlyBudgets(userId, currentMonth, currentYear);
            budgetsTable.setItems(FXCollections.observableArrayList(budgets));

            // Update summary
            updateSummary();

        } catch (Exception e) {
            showAlert("Error", "Failed to load data: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateSummary() {
        try {
            int userId = authManager.getCurrentUser().getId();

            double income = transactionService.calculateMonthlyIncome(userId, currentMonth, currentYear);
            double expense = transactionService.calculateMonthlyExpense(userId, currentMonth, currentYear);
            double balance = income - expense;

            incomeLabel.setText(String.format("$%.2f", income));
            expenseLabel.setText(String.format("$%.2f", expense));
            balanceLabel.setText(String.format("$%.2f", balance));

        } catch (Exception e) {
            showAlert("Error", "Failed to update summary: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/a2/fxml/add-transaction.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Transaction");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshData();
        } catch (IOException e) {
            showAlert("Error", "Failed to open transaction form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteTransaction() {
        Transaction selected = transactionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a transaction to delete", Alert.AlertType.WARNING);
            return;
        }

        try {
            transactionService.deleteTransaction(selected.getId());
            refreshData();
        } catch (Exception e) {
            showAlert("Error", "Failed to delete transaction: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSetBudget() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/a2/fxml/set-budget.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Set Budget");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            refreshData();
        } catch (IOException e) {
            showAlert("Error", "Failed to open budget form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGenerateReport() {
        try {
            int month = monthComboBox.getSelectionModel().getSelectedIndex() + 1;
            int year = yearComboBox.getValue();
            int userId = authManager.getCurrentUser().getId();

            StringBuilder report = new StringBuilder();
            report.append("========================================\n");
            report.append(String.format("   MONTHLY REPORT - %s %d\n", monthComboBox.getValue(), year));
            report.append("========================================\n\n");

            double income = transactionService.calculateMonthlyIncome(userId, month, year);
            double expense = transactionService.calculateMonthlyExpense(userId, month, year);

            report.append(String.format("Total Income:  $%.2f\n", income));
            report.append(String.format("Total Expense: $%.2f\n", expense));
            report.append(String.format("Net Balance:   $%.2f\n\n", income - expense));

            report.append("CATEGORY BREAKDOWN:\n");
            report.append("----------------------------------------\n");

            List<Transaction> transactions = transactionService.getMonthlyTransactions(userId, month, year);
            transactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Transaction::getCategoryName,
                    java.util.stream.Collectors.summingDouble(Transaction::getAmount)
                ))
                .forEach((category, amount) ->
                    report.append(String.format("%-20s $%.2f\n", category, amount))
                );

            reportTextArea.setText(report.toString());

        } catch (Exception e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        authManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/a2/fxml/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            showAlert("Error", "Failed to logout: " + e.getMessage(), Alert.AlertType.ERROR);
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
