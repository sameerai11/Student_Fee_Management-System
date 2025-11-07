package dao;
import utility.DBConnection;
import model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // --- SQL Query for Foreign Key Check on 'studentfees' table ---
    private static final String COUNT_ASSIGNED_FEES =
            "SELECT COUNT(*) FROM studentfees WHERE student_id = ?";

    // --- CREATE (Add Student) ---
    public int addStudent(Student student) throws SQLException {
        // Query excludes 'user_id'
        String sql = "INSERT INTO students (first_name, last_name, department, email, phone_number) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, student.getFirstName());
            stmt.setString(2, student.getLastName());
            stmt.setString(3, student.getDepartment());
            stmt.setString(4, student.getEmail());
            stmt.setString(5, student.getPhoneNumber());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        student.setStudentId(newId);
                        return newId;
                    }
                }
            }
            return -1; // Failed to insert or retrieve ID
        }
    }

    // --- READ (Get All Students) ---
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        // Query excludes 'user_id'
        String sql = "SELECT student_id, first_name, last_name, department, email, phone_number FROM students";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = new Student(
                        rs.getInt("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("department"),
                        rs.getString("email"),
                        rs.getString("phone_number")
                );
                students.add(student);
            }
        }
        return students;
    }

    // --- READ (Get Student by ID) ---
    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT student_id, first_name, last_name, department, email, phone_number FROM students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("student_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("department"),
                            rs.getString("email"),
                            rs.getString("phone_number")
                    );
                }
            }
        }
        return null; // Return null if no student is found with that ID
    }

    // --- UPDATE (Edit Student) ---
    public boolean updateStudent(Student student) throws SQLException {
        // Query excludes 'user_id' from the SET clause
        String sql = "UPDATE students SET first_name = ?, last_name = ?, department = ?, email = ?, phone_number = ? WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getFirstName());
            stmt.setString(2, student.getLastName());
            stmt.setString(3, student.getDepartment());
            stmt.setString(4, student.getEmail());
            stmt.setString(5, student.getPhoneNumber());

            stmt.setInt(6, student.getStudentId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a Student record. Checks for dependent records in 'studentfees' first.
     * @param studentId The ID of the student to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws IllegalStateException if the student has assigned fee obligations.
     */
    public boolean deleteStudent(int studentId) throws SQLException {
        // 1. Check for dependent records (Foreign Key Check)
        if (isStudentAssignedFees(studentId)) {
            // Throw an exception that the UI can catch and display a clean message
            throw new IllegalStateException("Cannot delete student. This student has assigned fee obligations.");
        }

        // 2. Proceed with deletion if no dependents are found
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Helper method to check if a Student has any fee records assigned in the studentfees table.
     */
    private boolean isStudentAssignedFees(int studentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_ASSIGNED_FEES)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // If count > 0, the student has assigned fees
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
}