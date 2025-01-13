package com.expensetracker.services;

import com.expensetracker.models.Expense;
import com.expensetracker.utils.DatabaseUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseService {
    private List<Expense> expenses = new ArrayList<>();
    private double budget = 0;

    public List<Expense> getExpenses() {
        expenses.clear(); 
        String query = "SELECT * FROM expenses";
        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Expense expense = new Expense(
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("date")
                );
                expenses.add(expense);
            }
        } catch (SQLException e) {
            System.err.println("Error while fetching expenses: " + e.getMessage());
            e.printStackTrace();  // Log more details
        }
        return expenses;
    }

    public void addExpense(Expense expense) {
        String insertSQL = "INSERT INTO expenses (category, amount, date) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, expense.getCategory());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setString(3, expense.getDate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error while adding expense: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeExpense(Expense expense) {
        String deleteSQL = "DELETE FROM expenses WHERE category = ? AND amount = ? AND date = ?";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
            stmt.setString(1, expense.getCategory());
            stmt.setDouble(2, expense.getAmount());
            stmt.setString(3, expense.getDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error while removing expense: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public double getTotalExpenses() {
        double total = 0;
        String query = "SELECT SUM(amount) AS total FROM expenses";
        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error while calculating total expenses: " + e.getMessage());
            e.printStackTrace();
        }
        return total;
    }

    public void setBudget(double budget) {
        this.budget = budget;
        String updateSQL = "INSERT OR REPLACE INTO budget (id, amount) VALUES (1, ?)";
        try (Connection conn = DatabaseUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
            stmt.setDouble(1, budget);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error while setting budget: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public double getBudget() {
        String query = "SELECT amount FROM budget WHERE id = 1";
        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                budget = rs.getDouble("amount");
            }
        } catch (SQLException e) {
            System.err.println("Error while retrieving budget: " + e.getMessage());
            e.printStackTrace();
        }
        return budget;
    }

    public boolean isOverBudget() {
        double totalExpenses = getTotalExpenses();
        return totalExpenses > budget; 
    }
    public void removeAllExpenses() {
        String deleteSQL = "DELETE FROM expenses";  
        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(deleteSQL);  
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
