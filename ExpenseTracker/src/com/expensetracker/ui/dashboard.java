package com.expensetracker.ui;

import com.expensetracker.models.Expense;
import com.expensetracker.services.ExpenseService;
import com.expensetracker.services.DatabaseInitializer;
import com.expensetracker.utils.FileUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.chart.PieChart;
import java.util.stream.Collectors;
import javafx.scene.layout.HBox;

public class dashboard extends Application {
    private ExpenseService expenseService = new ExpenseService();
    private PieChart expenseChart = new PieChart();

    @Override
    public void start(Stage primaryStage) {
        DatabaseInitializer.initialize();    

        // Load the saved expenses from the database
        expenseService.getExpenses().addAll(FileUtils.loadExpenses());

        // Load the current budget from the database
        double currentBudget = expenseService.getBudget(); // Retrieve saved budget from database

        Label label = new Label("Expense Tracker");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        Label totalExpensesLabel = new Label("Total Expenses: $" + expenseService.getTotalExpenses());
        Button addButton = new Button("Add Expense");

        // Change ListView to hold HBox (which contains the expense label and delete button)
        ListView<HBox> expenseList = new ListView<>();

        // Populate ListView with loaded expenses
        expenseService.getExpenses().forEach(exp -> {
            String itemText = exp.getCategory() + ": $" + exp.getAmount();
            
            // Create delete button
            Button deleteButton = new Button("Delete");
            deleteButton.setOnAction(event -> {
                expenseService.removeExpense(exp);
                expenseList.getItems().removeIf(hbox -> {
                    Label labelInHbox = (Label) ((HBox) hbox).getChildren().get(0); // Get the label inside the HBox
                    return labelInHbox.getText().equals(itemText);
                });
                updateChart();
            });

            // Create a horizontal box to hold the expense label and the delete button
            HBox hbox = new HBox(10, new Label(itemText), deleteButton);
            expenseList.getItems().add(hbox);
        });

        addButton.setOnAction(e -> {
            try {
                if (expenseService.getBudget() == 0) {  // Check if budget is set
                    showAlert(Alert.AlertType.WARNING, "No Budget Set", "Please set a budget before adding expenses.");
                    return;  // Exit the method if no budget is set
                }

                String category = categoryField.getText();
                double amount = Double.parseDouble(amountField.getText());
                Expense expense = new Expense(category, amount, "2025-01-01"); 
                expenseService.addExpense(expense);
                
              
                String itemText = category + ": $" + amount;
                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(event -> {
                    expenseService.removeExpense(expense);
                    expenseList.getItems().removeIf(hbox -> {
                        Label labelInHbox = (Label) ((HBox) hbox).getChildren().get(0); // Get the label inside the HBox
                        return labelInHbox.getText().equals(itemText);
                    });
                    updateChart();
                });
                HBox hbox = new HBox(10, new Label(itemText), deleteButton);
                expenseList.getItems().add(hbox);

                totalExpensesLabel.setText("Total Expenses: $" + expenseService.getTotalExpenses());
                updateChart();

              
                if (expenseService.isOverBudget()) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "You have exceeded your budget!");
                }

               
                categoryField.clear();
                amountField.clear();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid inputs.");
            }
        });


        TextField budgetField = new TextField();
        budgetField.setPromptText("Set Monthly Budget");
        Button setBudgetButton = new Button("Set Budget");

       
        Label currentBudgetLabel = new Label("Current Budget: $" + currentBudget); 

     
        setBudgetButton.setOnAction(e -> {
            try {
                double budget = Double.parseDouble(budgetField.getText());
                expenseService.setBudget(budget);
                showAlert(Alert.AlertType.INFORMATION, "Budget Set", "Your budget has been set to $" + budget);
                
                // Update the current budget label
                currentBudgetLabel.setText("Current Budget: $" + budget); // Set the updated budget to label
                
                budgetField.clear();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for the budget.");
            }
        });

      
        Button resetButton = new Button("Reset Expenses");
        resetButton.setOnAction(e -> {
            expenseService.getExpenses().clear();  
            totalExpensesLabel.setText("Total Expenses: $0");  
            expenseService.removeAllExpenses(); 
            updateChart(); 
        });

        
        primaryStage.setOnCloseRequest(e -> FileUtils.saveExpenses(expenseService.getExpenses()));

        
        VBox layout = new VBox(10, 
            label, 
            categoryField, 
            amountField, 
            addButton, 
            budgetField, 
            setBudgetButton, 
            totalExpensesLabel, 
            currentBudgetLabel, 
            resetButton,  
            expenseList, 
            expenseChart
        );
        Scene scene = new Scene(layout, 400, 300);

        primaryStage.setTitle("Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateChart() {
        expenseChart.getData().clear();
        expenseService.getExpenses().stream()
            .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)))
            .forEach((category, total) -> expenseChart.getData().add(new PieChart.Data(category, total)));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
