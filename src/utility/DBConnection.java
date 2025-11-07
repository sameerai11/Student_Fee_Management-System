package utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Connection Parameters (Already Correct)
    // NOTE: Ensure your MySQL service is running on localhost:3306
    private static final String URL = "jdbc:mysql://localhost:3306/student_fees_db?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "root@123";

    /**
     * Establishes and returns a connection to the MySQL database.
     * This method is static, allowing it to be called directly on the class: DBConnection.getConnection()
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL driver (necessary for older Java versions, good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection using the defined parameters
            return DriverManager.getConnection(URL, USER, PASS);

        } catch (ClassNotFoundException e) {
            // This error indicates the MySQL Connector JAR is not properly set up in IntelliJ
            System.err.println("MySQL JDBC Driver not found! Check your project libraries.");
            throw new SQLException("JDBC Driver not available.", e);
        }
    }
}
