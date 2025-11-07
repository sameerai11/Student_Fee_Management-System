package ui;

import dao.UserDAO;
import model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    // --- Core Scene Switching Method ---
    private void switchScene(ActionEvent event, String fxmlFileName, String title, User user) throws IOException {
        // 1. Load the FXML file for the next scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Parent root = loader.load();

        // --- NEW/MODIFIED: Pass user data to the next controller if needed ---
        if ("StudentUI.fxml".equals(fxmlFileName)) {
            StudentController studentController = loader.getController();
            studentController.initData(user);
        }
        // (AdminController does not currently require initData, but logic can be added here)


        // 2. Get the current stage (window) from the button/event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // 3. *** CRITICAL FIX: DETERMINE SCREEN BOUNDS ***
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // 4. Set the new scene using the full screen dimensions
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());

        stage.setTitle(title);
        stage.setScene(scene);

        // 5. Explicitly apply maximization again
        stage.setMaximized(true);

        stage.show();
    }

    // --- Login Authentication and Routing ---
    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            UserDAO userDAO = new UserDAO();
            User authenticatedUser = userDAO.login(username, password);

            if (authenticatedUser != null) {
                String role = authenticatedUser.getRole();

                if ("admin".equalsIgnoreCase(role)) {
                    // Successful Admin Login: Switch to AdminUI
                    // NOTE: Pass null for 'user' as AdminController currently doesn't require initData.
                    switchScene(event, "AdminUI.fxml", "Admin Dashboard - Student Fee Manager", null);

                } else if ("student".equalsIgnoreCase(role)) {
                    // *** SUCCESSFUL STUDENT LOGIN: ROUTE TO StudentUI AND PASS USER DATA ***
                    switchScene(event, "StudentUI.fxml", "Student Portal - Fee Manager", authenticatedUser);

                } else {
                    // Should not happen if roles are enforced, but good for safety
                    showAlert(AlertType.ERROR, "Authentication Error", "User role is unrecognized.");
                }

            } else {
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error", "Failed to connect or query the database. Check console.");
            e.printStackTrace();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "UI Load Error", "Could not load the next screen (" + e.getMessage() + ").");
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}