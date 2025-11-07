package model;

// Use java.lang.Integer because studentId is nullable in the database
import java.lang.Integer;

public class User {

    // Fields corresponding to the Users table
    private int userId;
    private String username;
    private String password; // Will store the HASHED password retrieved from DB
    private String role;     // 'admin' or 'student'
    private Integer studentId; // Can be NULL, so use Integer object, not int primitive

    // -------------------------------------------------------------------
    // 1. Constructors
    // -------------------------------------------------------------------

    // Default Constructor (required by some frameworks)
    public User() {
    }

    // Parameterized Constructor (for creating a User object after login)
    public User(int userId, String username, String password, String role, Integer studentId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.studentId = studentId;
    }

    // -------------------------------------------------------------------
    // 2. Getters and Setters
    // -------------------------------------------------------------------

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Getter and Setter for the HASHED password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getStudentId() {
        return studentId;
    }

    // Setter uses Integer to handle null values from the database
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
}