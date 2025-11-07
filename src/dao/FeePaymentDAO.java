package dao;

import model.FeePayment;
import utility.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Data Access Object for FeePayment (Transaction) records.
 */
public class FeePaymentDAO {

    // --- SQL Statements ---

    // CRITICAL FIX: Changed column names and table name to match 'transactions' table schema:
    // Uses: transaction_id, payment_amount, transaction_date.
    private static final String SELECT_PAYMENTS_BY_STUDENT_FEE_ID =
            "SELECT transaction_id, student_fee_id, payment_amount, transaction_date, payment_method " +
                    "FROM transactions WHERE student_fee_id = ?";

    // NEW SQL: Select all payments for Admin's transaction view
    private static final String SELECT_ALL_PAYMENTS =
            "SELECT transaction_id, student_fee_id, payment_amount, transaction_date, payment_method " +
                    "FROM transactions ORDER BY transaction_date DESC";

    private static final String INSERT_FEE_PAYMENT =
            "INSERT INTO transactions (student_fee_id, payment_amount, payment_method) VALUES (?, ?, ?)";
    // NOTE: transaction_date is handled by the database using CURRENT_TIMESTAMP.


    /**
     * Helper method to map a ResultSet row to a FeePayment object.
     * * @param rs The ResultSet from the database query.
     * @return A fully populated FeePayment object.
     * @throws SQLException If a database access error occurs.
     */
    private FeePayment mapResultSetToFeePayment(ResultSet rs) throws SQLException {
        FeePayment payment = new FeePayment();

        // 1. Map transaction_id (DB) to paymentId (Model)
        payment.setPaymentId(rs.getInt("transaction_id"));

        payment.setStudentFeeId(rs.getInt("student_fee_id"));

        // 2. Map payment_amount (DB) to amount (Model)
        payment.setAmount(rs.getBigDecimal("payment_amount"));

        // 3. Map transaction_date (DB) to paymentDate (Model: LocalDate)
        Date sqlDate = rs.getDate("transaction_date");
        if (sqlDate != null) {
            payment.setPaymentDate(sqlDate.toLocalDate());
        }

        payment.setPaymentMethod(rs.getString("payment_method"));

        // NOTE: The 'receiptNumber' field in the model is not supported by the current 'transactions' table schema.

        return payment;
    }

    /**
     * Records a new fee payment transaction in the database.
     * * @param payment The FeePayment object containing the payment details.
     * @return The auto-generated transaction_id, or -1 on failure.
     * @throws SQLException If a database error occurs.
     */
    public int addPayment(FeePayment payment) throws SQLException {
        int transactionId = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_FEE_PAYMENT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payment.getStudentFeeId());
            stmt.setBigDecimal(2, payment.getAmount());
            stmt.setString(3, payment.getPaymentMethod());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        transactionId = rs.getInt(1);
                        payment.setPaymentId(transactionId);
                    }
                }
            }
        }
        return transactionId;
    }

    /**
     * Retrieves all fee payments (transactions) for a specific student fee assignment ID.
     * This is used by the StudentController.
     * * @param studentFeeId The ID of the assigned fee.
     * @return A list of FeePayment objects.
     * @throws SQLException If a database error occurs.
     */
    public List<FeePayment> getPaymentsByStudentFeeId(int studentFeeId) throws SQLException {
        List<FeePayment> payments = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PAYMENTS_BY_STUDENT_FEE_ID)) {

            stmt.setInt(1, studentFeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToFeePayment(rs));
                }
            }
        }
        return payments;
    }

    /**
     * Retrieves ALL fee payments (transactions) from the database for the Admin view.
     * * @return A list of all FeePayment objects, ordered by date.
     * @throws SQLException If a database error occurs.
     */
    public List<FeePayment> getAllPayments() throws SQLException {
        List<FeePayment> payments = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_PAYMENTS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(mapResultSetToFeePayment(rs));
            }
        }
        return payments;
    }
}