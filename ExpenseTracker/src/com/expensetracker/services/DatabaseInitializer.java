package com.expensetracker.services;

import com.expensetracker.utils.DatabaseUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
     
        String createExpensesTableSQL = "CREATE TABLE IF NOT EXISTS expenses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "date TEXT NOT NULL" +
                ");";

      
        String createBudgetTableSQL = "CREATE TABLE IF NOT EXISTS budget (" +
                "id INTEGER PRIMARY KEY, " +
                "amount REAL NOT NULL" +
                ");";

        try (Connection conn = DatabaseUtils.getConnection();
             Statement stmt = conn.createStatement()) {
    
            stmt.execute(createExpensesTableSQL);
            stmt.execute(createBudgetTableSQL);

           
            String insertDefaultBudgetSQL = "INSERT OR IGNORE INTO budget (id, amount) VALUES (1, 0)";
            stmt.execute(insertDefaultBudgetSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
