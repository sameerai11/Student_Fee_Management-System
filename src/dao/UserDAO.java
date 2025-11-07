package dao;

import model.User;
import utility.DBConnection;
import utility.PasswordHasher;
import java.sql.*;

public class UserDAO {

    // SQL query to retrieve a user's hashed password and role based on username
    private static final String SELECT_USER_BY_USERNAME =
            "SELECT user_id, password, role, student_id FROM Users WHERE username = ?";

    // New SQL query to retrieve user details based on user ID
    private static final String SELECT_USER_BY_ID =
            "SELECT user_id, username, password, role, student_id FROM Users WHERE user_id = ?";

    // SQL query to insert a new user (with hashed password)
    private static final String INSERT_USER =
            "INSERT INTO Users (username, password, role, student_id) VALUES (?, ?, ?, ?)";


    /**
     * Attempts to log in a user.
     * @param username The username entered by the user.
     * @param plainTextPassword The raw password entered by the user.
     * @return A populated User object if login is successful, or null otherwise.
     * @throws SQLException If a database error occurs.
     */
    public User login(String username, String plainTextPassword) throws SQLException {
        User user = null;

        // Use try-with-resources for automatic closing of Connection and PreparedStatement
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_USER_BY_USERNAME)) {

            // Set the username parameter for the query
            stmt.setString(1, username);

            // Execute the query
            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    String hashedPassword = rs.getString("password");

                    // 1. Verify the password hash using the BCrypt utility
                    // FIX: Changed verifyPassword to the correct method: checkPassword
                    if (PasswordHasher.checkPassword(plainTextPassword, hashedPassword)) {

                        // 2. Password is correct. Build the User object from the result set.

                        // Handle the nullable student_id from the database:
                        int studentIdPrimitive = rs.getInt("student_id");
                        // Convert primitive int to Integer object, handling DB NULL values correctly
                        Integer studentIdObject = rs.wasNull() ? null : studentIdPrimitive;

                        user = new User(
                                rs.getInt("user_id"),
                                username,
                                hashedPassword, // Store the hash
                                rs.getString("role"),
                                studentIdObject
                        );
                    }
                }
            }
        }
        return user; // Returns null if user not found or password verification failed
    }

    /**
     * Adds a new user to the database, hashing the password before storage.
     * @param user The User object containing username, plain-text password, and role.
     * @return The auto-generated user_id of the new user, or -1 on failure.
     * @throws SQLException If a database error occurs.
     */
    public int addUser(User user) throws SQLException {
        int userId = -1;

        // 1. HASH THE PASSWORD before storing it in the database
        String hashedPassword = PasswordHasher.hashPassword(user.getPassword());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashedPassword); // Store the hash
            stmt.setString(3, user.getRole());

            // Handle nullable studentId:
            if (user.getStudentId() == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, user.getStudentId());
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                        // Update the user object with the new ID and HASHED password
                        user.setUserId(userId);
                        user.setPassword(hashedPassword);
                    }
                }
            }
        }
        return userId;
    }

    /**
     * Retrieves a user object based on their unique user ID.
     * This is useful for loading full user details after authentication.
     * @param userId The unique ID of the user.
     * @return A populated User object if found, or null otherwise.
     * @throws SQLException If a database error occurs.
     */
    public User getUserById(int userId) throws SQLException {
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_USER_BY_ID)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Handle the nullable student_id from the database:
                    int studentIdPrimitive = rs.getInt("student_id");
                    Integer studentIdObject = rs.wasNull() ? null : studentIdPrimitive;

                    user = new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"), // Hash is stored here
                            rs.getString("role"),
                            studentIdObject
                    );
                }
            }
        }
        return user;
    }
}
