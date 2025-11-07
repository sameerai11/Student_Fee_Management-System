package dao;

import model.FeeTemplate;
import utility.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Data Access Object for managing Fee Templates (the structure/type of a fee).
 * FIXED to match the 'fees' table schema and include Foreign Key check for deletion.
 */
public class FeeTemplateDAO {

    // --- SQL Queries (FIXED to use 'fees', 'fee_id', 'fee_name', 'amount') ---
    private static final String INSERT_FEE =
            "INSERT INTO fees (fee_name, amount) VALUES (?, ?)";

    private static final String SELECT_ALL_FEES =
            "SELECT fee_id, fee_name, amount FROM fees ORDER BY fee_name";

    private static final String SELECT_FEE_BY_ID =
            "SELECT fee_id, fee_name, amount FROM fees WHERE fee_id = ?";

    private static final String UPDATE_FEE =
            "UPDATE fees SET fee_name = ?, amount = ? WHERE fee_id = ?";

    private static final String DELETE_FEE =
            "DELETE FROM fees WHERE fee_id = ?";

    // --- NEW: Query to check if the fee is used by any student ---
    private static final String COUNT_ASSIGNED_FEES =
            "SELECT COUNT(*) FROM studentfees WHERE fee_id = ?";


    // --- Helper Method (FIXED mapping) ---

    private FeeTemplate mapResultSetToFeeTemplate(ResultSet rs) throws SQLException {
        return new FeeTemplate(
                rs.getInt("fee_id"),
                rs.getString("fee_name"),
                rs.getBigDecimal("amount")
        );
    }

    // --- CRUD Methods ---

    /**
     * Adds a new Fee Template to the database.
     * @param feeTemplate The FeeTemplate object containing name and amount.
     * @return The auto-generated fee_id of the new template, or -1 on failure.
     */
    public int addFee(FeeTemplate feeTemplate) throws SQLException {
        int newId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_FEE, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, feeTemplate.getName());
            stmt.setBigDecimal(2, feeTemplate.getDefaultAmount());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newId = rs.getInt(1);
                        feeTemplate.setFeeTemplateId(newId);
                    }
                }
            }
        }
        return newId;
    }

    /**
     * Retrieves a single Fee Template from the database by ID.
     * @param feeTemplateId The ID of the Fee Template to retrieve.
     * @return The FeeTemplate object, or null if not found.
     * @throws SQLException If a database error occurs.
     */
    public FeeTemplate getFeeById(int feeTemplateId) throws SQLException {
        FeeTemplate template = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_FEE_BY_ID)) {

            stmt.setInt(1, feeTemplateId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    template = mapResultSetToFeeTemplate(rs);
                }
            }
        }
        return template;
    }

    /**
     * Retrieves all Fee Templates from the database.
     * @return A list of FeeTemplate objects.
     * @throws SQLException If a database error occurs.
     */
    public List<FeeTemplate> getAllFees() throws SQLException {
        List<FeeTemplate> templates = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_FEES);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                templates.add(mapResultSetToFeeTemplate(rs));
            }
        }
        return templates;
    }

    /**
     * Updates an existing Fee Template in the database.
     * @param feeTemplate The FeeTemplate object with updated details.
     * @return true if the update was successful, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    public boolean updateFee(FeeTemplate feeTemplate) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_FEE)) {

            stmt.setString(1, feeTemplate.getName());
            stmt.setBigDecimal(2, feeTemplate.getDefaultAmount());
            stmt.setInt(3, feeTemplate.getFeeTemplateId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a Fee Template from the database by ID.
     * **Includes a check for existing student fees to prevent Foreign Key errors.**
     * * @param feeTemplateId The ID of the Fee Template to delete.
     * @return true if deletion was successful, false otherwise.
     * @throws SQLException If a database error occurs.
     * @throws IllegalStateException If the fee is currently assigned to a student.
     */
    public boolean deleteFee(int feeTemplateId) throws SQLException {
        // 1. Check for dependent records (Foreign Key Check)
        if (isFeeAssigned(feeTemplateId)) {
            // Throw an exception that the UI can catch and display a clean message
            throw new IllegalStateException("Cannot delete this Fee Template. It has already been assigned to one or more students.");
        }

        // 2. Proceed with deletion if no dependents are found
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_FEE)) {

            stmt.setInt(1, feeTemplateId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Helper method to check if a Fee Template is currently assigned to any student.
     */
    private boolean isFeeAssigned(int feeTemplateId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_ASSIGNED_FEES)) {

            stmt.setInt(1, feeTemplateId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // If count > 0, the fee is assigned
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
}