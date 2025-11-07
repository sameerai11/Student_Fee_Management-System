package dao;

import model.Transaction;
import utility.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class TransactionDAO {

    // FIX: Changed table name from 'Transactions' to 'transactions' to match DB schema
    private static final String INSERT_TRANSACTION =
            "INSERT INTO transactions (student_fee_id, payment_amount, transaction_date, payment_method) VALUES (?, ?, NOW(), ?)";

    // FIX: Changed table name from 'Transactions' to 'transactions' to match DB schema
    private static final String SELECT_ALL_TRANSACTIONS =
            "SELECT * FROM transactions ORDER BY transaction_date DESC";

    // FIX: Changed table name from 'Transactions' to 'transactions' to match DB schema
    private static final String SELECT_TRANSACTIONS_BY_STUDENT_FEE_ID =
            "SELECT * FROM transactions WHERE student_fee_id = ? ORDER BY transaction_date DESC";


    // Helper method to map a ResultSet row to a Transaction object - CORRECTED
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("transaction_id"),
                rs.getInt("student_fee_id"), // CORRECTED FIELD
                rs.getBigDecimal("payment_amount"),
                rs.getTimestamp("transaction_date"),
                rs.getString("payment_method")
        );
    }

    /**
     * Records a new payment transaction and returns the generated transaction ID.
     */
    public int recordPayment(Transaction transaction) throws SQLException {
        int transactionId = -1;
        try (Connection conn = DBConnection.getConnection();
             // Use Statement.RETURN_GENERATED_KEYS to retrieve the primary key
             PreparedStatement stmt = conn.prepareStatement(INSERT_TRANSACTION, Statement.RETURN_GENERATED_KEYS)) {

            // Parameter 1: student_fee_id
            stmt.setInt(1, transaction.getStudentFeeId());
            // Parameter 2: payment_amount
            stmt.setBigDecimal(2, transaction.getPaymentAmount());
            // Parameter 3: payment_method (transaction_date is set by NOW() in SQL)
            stmt.setString(3, transaction.getPaymentMethod());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        transactionId = rs.getInt(1);
                        transaction.setTransactionId(transactionId);
                    }
                }
            }
        }
        return transactionId;
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_TRANSACTIONS)) {

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    /**
     * Retrieves transactions based on the Student Fee Assignment ID.
     */
    public List<Transaction> getTransactionsByStudentFeeId(int studentFeeId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_TRANSACTIONS_BY_STUDENT_FEE_ID)) {

            stmt.setInt(1, studentFeeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
        }
        return transactions;
    }
}
