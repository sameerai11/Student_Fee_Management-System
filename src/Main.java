
import javafx.application.Application;
import ui.LoginApp;        // your JavaFX Application subclass
import utility.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        System.out.println("==========================================");
        System.out.println("   \uD83C\uDF93 Student Fee Management System (JavaFX)");
        System.out.println("==========================================");
        System.out.println("Checking database connection...");

        try (Connection conn = DBConnection.getConnection()) {

            if (conn != null) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("⚠️  Database connection returned null. Proceeding with UI launch...");
            }

        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("Details: " + e.getMessage());
            System.err.println("Proceeding to launch UI in offline mode...");
        }

        // Launch the JavaFX Login window
        try {
            System.out.println("🚀 Launching JavaFX Application...");
            Application.launch(LoginApp.class, args);
        } catch (Exception e) {
            System.err.println("⚠️  Failed to start JavaFX application.");
            e.printStackTrace();
        }

        System.out.println("==========================================");
        System.out.println("   Application terminated.");
        System.out.println("==========================================");
    }
}
