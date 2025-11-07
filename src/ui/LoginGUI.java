package ui;

import dao.UserDAO;
import model.User; // ADDED THIS IMPORT to use the User object returned by login()
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class LoginGUI extends JFrame implements ActionListener {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;

    public LoginGUI() {
        // --- 1. Frame Setup ---
        super("Student Fee Management Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null); // Center the window

        // --- 2. Component Initialization ---
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        loginButton = new JButton("Login");
        loginButton.addActionListener(this); // Register for button clicks

        // --- 3. Layout Setup (Using GridLayout for simplicity) ---
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10)); // 3 rows, 2 columns, 10px spacing
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        // Blank space for alignment
        panel.add(new JLabel(""));
        panel.add(loginButton);

        // --- 4. Finalizing Frame ---
        add(panel, BorderLayout.CENTER);
        setVisible(true); // Display the window
    }

    // --- 5. Event Handling (Authentication Logic) ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            try {
                UserDAO userDAO = new UserDAO();

                // CORRECT CALL: Call the existing 'login' method, which returns a User object
                User authenticatedUser = userDAO.login(username, password);

                if (authenticatedUser != null) {
                    // Extract the role from the successfully authenticated User object
                    String role = authenticatedUser.getRole();

                    JOptionPane.showMessageDialog(this, "Login Successful! Role: " + role, "Success", JOptionPane.INFORMATION_MESSAGE);

                    // TODO: Close this window and launch the appropriate GUI based on role
                    this.dispose();

                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                // Handle database connection or query errors
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}