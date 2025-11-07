package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;

public class AdminController {

    // FXML ID for the root BorderPane (linked from AdminUI.fxml)
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize() {
        System.out.println("Admin Dashboard initialized.");
        // Optional: Load the default Dashboard welcome view here
    }

    // Helper method to load new FXML into the center of the BorderPane
    private void loadCenterScene(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent view = loader.load();
            mainBorderPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load scene: " + fxmlFileName);
        }
    }

    // Handler for the "Dashboard" button (reloads the initial welcome screen)
    @FXML
    private void handleDashboard() {
        System.out.println("Switching to Dashboard view.");
        // You would typically load a separate Dashboard FXML here, e.g., loadCenterScene("DashboardWelcomeUI.fxml");
    }

    // Handler for the "Manage Students" button
    @FXML
    private void handleManageStudents() {
        loadCenterScene("StudentManagementUI.fxml");
    }

    // Handler for the "Manage Fees" button
    @FXML
    private void handleManageFees() {
        loadCenterScene("FeeManagementUI.fxml");
    }

    // Handler for the "View Transactions" button (CORRECTED to load TransactionUI.fxml)
    @FXML
    private void handleViewTransactions() {
        System.out.println("Switching to View Transactions view.");
        loadCenterScene("TransactionUI.fxml");
    }


    // Method linked to the Logout button in AdminUI.fxml
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logging out...");

        // Get the current stage (window)
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Close the current window
        stage.close();
    }
}