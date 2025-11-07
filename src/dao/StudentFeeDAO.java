package dao;

import model.StudentFee;
import utility.DBConnection;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing StudentFee records.
 * FIXED to match the confirmed database schema (NO amount_paid or balance_due columns).
 * The payment functionality (recordPayment) is removed as it relies on missing columns.
 */
public class StudentFeeDAO {

    // --- SQL Statements (FIXED to match your confirmed studentfees table schema) ---

    private static final String SELECT_ALL_STUDENT_FEES =
            "SELECT student_fee_id, fee_id, student_id, fee_amount, due_date, status, description FROM studentfees";

    // CRITICAL FIX: Removed amount_paid and balance_due
    private static final String INSERT_STUDENT_FEE =
            "INSERT INTO studentfees (student_id, fee_id, fee_amount, due_date, status, description) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_FEES_BY_STUDENT_ID =
            "SELECT student_fee_id, fee_id, student_id, fee_amount, due_date, status, description FROM studentfees WHERE student_id = ?";

    // CRITICAL FIX: Simplified the update status to only use status (no amount_paid/balance_due)
    private static final String UPDATE_STATUS =
            "UPDATE studentfees SET status = ? WHERE student_fee_id = ?";


    /**
     * Helper method to map a ResultSet row to a StudentFee object.
     * @param rs The ResultSet from the database query.
     * @return A fully populated StudentFee object.
     * @throws SQLException If a database access error occurs.
     */
    private StudentFee mapResultSetToStudentFee(ResultSet rs) throws SQLException {
        // Retrieves the fee_id.
        int feeId = rs.getInt("fee_id");
        if (rs.wasNull()) {
            feeId = 1; // Default to a known ID if fee_id is null/not available yet
        }

        StudentFee studentFee = new StudentFee();
        studentFee.setStudentFeeId(rs.getInt("student_fee_id"));
        studentFee.setFeeId(feeId);
        studentFee.setStudentId(rs.getInt("student_id"));
        studentFee.setFeeAmount(rs.getBigDecimal("fee_amount"));
        // NOTE: amount_paid is NOT retrieved as it is not in the DB
        studentFee.setDueDate(rs.getDate("due_date"));
        studentFee.setStatus(rs.getString("status"));
        studentFee.setDescription(rs.getString("description"));

        return studentFee;
    }

    /**
     * Retrieves all assigned student fee records from the database.
     * @return A list of all StudentFee objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<StudentFee> getAllStudentFees() throws SQLException {
        List<StudentFee> fees = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_STUDENT_FEES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                fees.add(mapResultSetToStudentFee(rs));
            }
        }

        return fees;
    }

    /**
     * Retrieves all fees assigned to a specific student ID.
     * @param studentId The ID of the student.
     * @return A list of StudentFee objects for the given student.
     * @throws SQLException If a database access error occurs.
     */
    public List<StudentFee> getStudentFeesByStudentId(int studentId) throws SQLException {
        List<StudentFee> fees = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_FEES_BY_STUDENT_ID)) {

            stmt.setInt(1, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fees.add(mapResultSetToStudentFee(rs));
                }
            }
        }
        return fees;
    }

    /**
     * Handles manual status updates.
     * NOTE: Since 'amount_paid' is missing, this method ONLY updates the 'status' column.
     * @param studentFeeId The ID of the fee assignment to update.
     * @param newStatus The desired new status (PAID, OUTSTANDING, OVERDUE).
     * @return true if the update was successful.
     * @throws SQLException If a database access error occurs.
     */
    public boolean manuallyUpdateStatus(int studentFeeId, String newStatus) throws SQLException {
        // CRITICAL FIX: We are using the simple UPDATE_STATUS query.
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STATUS)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, studentFeeId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Inserts a new fee record into the studentfees table.
     * CRITICAL FIX: Removed amount_paid parameter binding.
     * @param studentFee The StudentFee object containing details for the new record.
     * @return The auto-generated student_fee_id, or -1 if the insertion failed.
     * @throws SQLException If a database access error occurs.
     */
    public int assignFee(StudentFee studentFee) throws SQLException {
        int newId = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_STUDENT_FEE, Statement.RETURN_GENERATED_KEYS)) {

            // Map the model properties to the SQL statement parameters (6 parameters total)
            stmt.setInt(1, studentFee.getStudentId());
            // Using a default of 1 if feeId is not set, as it must be a valid ID
            stmt.setInt(2, studentFee.getFeeId() == 0 ? 1 : studentFee.getFeeId());
            stmt.setBigDecimal(3, studentFee.getFeeAmount());
            // Parameter 4 (Date)
            stmt.setDate(4, studentFee.getDueDate());
            // Parameter 5 (Status)
            stmt.setString(5, studentFee.getStatus());
            // Parameter 6 (Description)
            stmt.setString(6, studentFee.getDescription());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newId = generatedKeys.getInt(1); // Retrieve the auto-generated primary key
                    }
                }
            }
        }
        return newId;
    }

    /*
     * DEPRECATED/REMOVED METHODS due to missing database columns:
     * - recordPayment()
     * - updateFeeStatus() (Replaced by manuallyUpdateStatus using the simpler query)
     * - SELECT_FEE_AMOUNT_FOR_UPDATE
     * - UPDATE_PAYMENT_AND_STATUS
     * - UPDATE_MANUAL_STATUS (Replaced by UPDATE_STATUS)
     */
}