package ui;

import dao.StudentFeeDAO;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.StudentFee;

import java.math.BigDecimal;
import java.sql.Date; // Use java.sql.Date for consistency with DAO/DB
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the Fee Assignment Dialog (FeeAssignmentDialog.fxml).
 * Handles assigning a new financial obligation to a student AND manual status updates.
 */
public class FeeAssignmentDialogController {

    // --- FXML elements for FEE ASSIGNMENT ---
    @FXML
    private TextField studentIdField;
    @FXML
    private ComboBox<String> feeTypeComboBox;
    @FXML
    private TextField amountDueField;
    @FXML
    private DatePicker dueDateField;
    @FXML
    private TextArea descriptionArea;

    // --- FXML elements for MANUAL STATUS UPDATE (New) ---
    @FXML
    private TextField feeRecordIdField; // For entering the ID of the existing record to update
    @FXML
    private ChoiceBox<String> statusChoiceBox; // For selecting the new status

    // --- General Feedback Label (Reused for both features) ---
    @FXML
    private Label statusMessageLabel;

    private Stage dialogStage;
    private boolean isOkClicked = false;

    // Instance of the DAO for database operations
    private final StudentFeeDAO studentFeeDAO = new StudentFeeDAO();

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // 1. Setup ComboBox for Fee Types (Assignment Section)
        List<String> feeTypes = Arrays.asList("Tuition", "Library Fee", "Sports Fee", "Miscellaneous");
        feeTypeComboBox.setItems(FXCollections.observableArrayList(feeTypes));
        feeTypeComboBox.getSelectionModel().selectFirst();

        // 2. Add listener to restrict amount field to valid currency format
        amountDueField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allows 0 or more digits, followed by an optional decimal point and up to 2 decimal places
            if (!newValue.matches("\\d*([\\.]\\d{0,2})?")) {
                amountDueField.setText(oldValue);
            }
        });

        // 3. Set a default due date
        dueDateField.setValue(LocalDate.now().plusMonths(1));

        // 4. Setup ChoiceBox for Status Update (New Logic)
        List<String> validStatuses = Arrays.asList("OUTSTANDING", "OVERDUE", "CANCELED", "PAID");
        // Changing "PENDING" to "OUTSTANDING" to match common financial statuses
        statusChoiceBox.setItems(FXCollections.observableArrayList(validStatuses));
        statusChoiceBox.setValue("OUTSTANDING"); // Default selection
    }

    /**
     * Sets the stage of this dialog.
     * @param dialogStage The dialog stage.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     * @return true if OK was clicked.
     */
    public boolean isOkClicked() {
        return isOkClicked;
    }

    /**
     * Called when the user clicks OK (Save) for a new fee assignment.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            try {
                // 1. Gather and convert data from fields
                int studentId = Integer.parseInt(studentIdField.getText().trim());
                // Using the ComboBox's index + 1 as a placeholder for 'feeId'
                int feeIdPlaceholder = feeTypeComboBox.getSelectionModel().getSelectedIndex() + 1;
                BigDecimal amountDue = new BigDecimal(amountDueField.getText());
                Date sqlDueDate = Date.valueOf(dueDateField.getValue());
                String description = descriptionArea.getText().trim();

                // 2. Create the new StudentFee object
                StudentFee newFee = new StudentFee();
                newFee.setStudentId(studentId);
                newFee.setFeeId(feeIdPlaceholder);
                newFee.setFeeAmount(amountDue);
                newFee.setDueDate(sqlDueDate);
                newFee.setDescription(description);
                // Use "OUTSTANDING" as the initial status
                newFee.setStatus("OUTSTANDING");

                // CRITICAL FIX: Removed the line below which was causing the "amount_paid" error
                // newFee.setAmountPaid(BigDecimal.ZERO);
                // The DAO no longer handles this field for insert.

                // 3. Save the new fee obligation via the DAO
                int newId = studentFeeDAO.assignFee(newFee);

                if (newId > 0) {
                    // Update status label for assignment success
                    statusMessageLabel.setText("✅ Fee assigned successfully! New Record ID: " + newId);
                    isOkClicked = true;
                    // Note: We don't close the dialog immediately to allow for potential manual updates
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failure", "Assignment Failed", "Database operation failed to return a new ID. Check if Student ID exists.");
                }

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Data Error", "Invalid ID or Amount Format", "The Student ID or Amount field contains invalid numeric data.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save fee obligation.", "A database error occurred: " + e.getMessage() + "\nEnsure the Student ID is valid.");
                e.printStackTrace();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not save fee obligation.", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Called when the user clicks 'Update Status Manually'.
     * Updates the status of an existing fee record directly.
     */
    @FXML
    private void handleManualStatusUpdate(ActionEvent event) {
        String feeRecordIdText = feeRecordIdField.getText();
        String newStatus = statusChoiceBox.getValue();

        if (feeRecordIdText == null || feeRecordIdText.isEmpty() || newStatus == null) {
            statusMessageLabel.setText("❌ Error: Enter a Fee Record ID and select a New Status.");
            return;
        }

        try {
            int feeRecordId = Integer.parseInt(feeRecordIdText.trim());

            // Call the DAO method to perform the manual status update
            boolean success = studentFeeDAO.manuallyUpdateStatus(feeRecordId, newStatus);

            if (success) {
                statusMessageLabel.setText("✅ Status updated successfully for Record ID " + feeRecordId + " to: " + newStatus + ".");
                feeRecordIdField.clear(); // Clear input field
            } else {
                statusMessageLabel.setText("⚠️ Update failed. Record ID " + feeRecordId + " not found.");
            }

        } catch (NumberFormatException e) {
            statusMessageLabel.setText("❌ Error: Invalid Fee Record ID format. Must be an integer.");
        } catch (SQLException e) {
            statusMessageLabel.setText("❌ Database Error during status update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called when the user clicks Cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields for fee assignment.
     * @return true if the input is valid.
     */
    private boolean isInputValid() {
        String errorMessage = "";
        String studentIdText = studentIdField.getText();

        if (studentIdText == null || studentIdText.trim().isEmpty()) {
            errorMessage += "No Student ID provided!\n";
        } else {
            try {
                int id = Integer.parseInt(studentIdText.trim());
                if (id <= 0) {
                    errorMessage += "Student ID must be positive.\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Invalid Student ID format (must be an integer)!\n";
            }
        }

        if (feeTypeComboBox.getSelectionModel().isEmpty()) {
            errorMessage += "No Fee Type selected!\n";
        }

        String amountText = amountDueField.getText();
        if (amountText == null || amountText.trim().isEmpty()) {
            errorMessage += "No Amount Due entered!\n";
        } else {
            try {
                BigDecimal amount = new BigDecimal(amountText);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage += "Amount Due must be greater than zero.\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Invalid amount (must be a number)!\n";
            }
        }

        if (dueDateField.getValue() == null) {
            errorMessage += "No Due Date selected!\n";
        } else if (dueDateField.getValue().isBefore(LocalDate.now())) {
            errorMessage += "Due Date cannot be in the past!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.WARNING, "Invalid Fields", "Please correct invalid fields.", errorMessage);
            return false;
        }
    }

    /**
     * Helper method to display JavaFX Alerts.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}