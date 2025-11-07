package ui;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;
import java.util.Scanner;

// IMPORT THE NEW UI CLASSES
import ui.AdminUI;
import ui.StudentUI;

public class LoginUI {

    private final Scanner scanner;
    private final UserDAO userDAO;

    // Constructor to initialize the scanner and DAO
    public LoginUI() {
        this.scanner = new Scanner(System.in);
        this.userDAO = new UserDAO();
    }

    // Main method to display the login prompt and handle authentication
    public void show() {

        System.out.println("\n=============================================");
        System.out.println("          STUDENT FEE MANAGEMENT SYSTEM");
        System.out.println("=============================================");

        // Loop to allow multiple login attempts
        while (true) {
            System.out.print("\nEnter Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine(); // Password input

            try {
                // Call the DAO to verify credentials
                User user = userDAO.login(username, password);

                if (user != null) {
                    System.out.println("\n✅ Login successful! Welcome, " + user.getUsername() + ".");

                    // Route to the correct main menu based on role
                    if ("admin".equalsIgnoreCase(user.getRole())) {

                        // LAUNCH ADMIN UI
                        new AdminUI(user).show();

                    } else if ("student".equalsIgnoreCase(user.getRole())) {

                        // LAUNCH STUDENT UI (Once implemented)
                        new StudentUI(user).show();
                    }

                    // Exit the login loop after successful login and menu completion
                    break;
                } else {
                    System.err.println("❌ Login failed: Invalid username or password.");
                }
            } catch (SQLException e) {
                System.err.println("❌ Database Error during login: " + e.getMessage());
                // In a real application, you might exit or wait here
            }
        }
    }
}