package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the FXML file that defines the UI layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Student Fee Management Login");

        // Define the scene size (initial size is now less important since it will be maximized)
        // I increased the initial size just in case maximization fails.
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);

        // *** NEW LINE: MAXIMIZE THE INITIAL WINDOW ***
        primaryStage.setMaximized(true);

        primaryStage.show();
    }
}