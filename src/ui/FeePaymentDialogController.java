package ui;

import dao.FeePaymentDAO;
import dao.StudentFeeDAO;
import model.FeePayment;
import model.StudentFee;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controller for the Fee Payment Dialog.
 * FIXED to perform a simple "Mark as PAID" operation, as the DB lacks 'amount_paid' tracking.
 */
public class FeePaymentDialogController {

    @FXML private TextField amountInput;
    @FXML private DatePicker paymentDatePicker;
    @FXML private TextField referenceInput; // Used for "receiptNumber" in model
    @FXML private Label studentFeeIdLabel;
    @FXML private Label balanceDueLabel; // This label is now unused/hidden

    private Stage dialogStage;
    private FeePayment newPayment;
    private StudentFee selectedFee;
    private boolean saved = false;

    private final FeePaymentDAO feePaymentDAO = new FeePaymentDAO();
    private final StudentFeeDAO studentFeeDAO = new StudentFeeDAO();

    /**
     * Initializes the controller. Sets the current date as the default payment date.
     */
    @FXML
    private void initialize() {
        paymentDatePicker.setValue(LocalDate.now());
        // CRITICAL: Since we cannot track balance, we must hide or simplify UI elements
        balanceDueLabel.setText("N/A - Full Amount Due: " + selectedFee.getFeeAmount().toString());
    }

    /**
     * Sets the stage of this dialog.
     * @param dialogStage The stage object.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the fee payment object to be edited/saved.
     * @param newPayment The FeePayment object (pre-populated with StudentFeeId).
     */
    public void setFeePayment(FeePayment newPayment) {
        this.newPayment = newPayment;
    }

    /**
     * Sets the student fee details for display and validation.
     * @param selectedFee The StudentFee object.
     */
    public void setStudentFeeDetails(StudentFee selectedFee) {
        this.selectedFee = selectedFee;
        studentFeeIdLabel.setText(String.valueOf(selectedFee.getStudentFeeId()));

        // CRITICAL FIX: Removed selectedFee.getBalanceDue() calls.
        // We now display the full fee amount and pre-fill the input with it.
        BigDecimal fullAmount = selectedFee.getFeeAmount();
        balanceDueLabel.setText("Amount Due: " + fullAmount.toString());
        amountInput.setText(fullAmount.toString());
    }

    /**
     * Returns true if the user clicked Save, false otherwise.
     * @return true if saved.
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * Handles the 'Save Payment' button action.
     * This is now a two-step process: 1) Save payment record, 2) Mark parent fee as PAID.
     */
    @FXML
    private void handleSavePayment() {
        if (isInputValid()) {
            try {
                // Get values from UI
                BigDecimal paymentAmount = new BigDecimal(amountInput.getText().trim());
                LocalDate localDate = paymentDatePicker.getValue();

                // Set payment details in the model object
                newPayment.setAmount(paymentAmount);
                newPayment.setPaymentDate(localDate);
                newPayment.setReceiptNumber(referenceInput.getText().trim());
                // Ensure newPayment has studentFeeId set, likely done by calling controller.

                // 1. Record the payment in the FeePayment table
                int paymentId = feePaymentDAO.addPayment(newPayment); // Assuming this method works

                if (paymentId > 0) {
                    // 2. Mark the parent StudentFee as fully PAID (simplest update possible)
                    // CRITICAL FIX: Use manuallyUpdateStatus instead of the removed recordPayment
                    boolean feeUpdated = studentFeeDAO.manuallyUpdateStatus(selectedFee.getStudentFeeId(), "PAID");

                    if (feeUpdated) {
                        saved = true;
                        dialogStage.close();
                    } else {
                        // Notify that payment was saved but status update failed
                        showAlert(Alert.AlertType.WARNING, "Partial Success", "Payment recorded, but fee status not updated.",
                                "The fee record ID " + selectedFee.getStudentFeeId() + " could not be marked as PAID.");
                        saved = true; // Still close if payment record was successful
                        dialogStage.close();
                    }
                } else {
                    throw new SQLException("Failed to save new payment record to the database (FeePaymentDAO issue).");
                }

            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "The amount must be a valid number.", e.getMessage());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to record payment or update fee.",
                        "Please check database connection and DAO methods. Error: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the 'Cancel' button action.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates user input fields.
     */
    private boolean isInputValid() {
        String errorMessage = "";
        String amountText = amountInput.getText();

        if (amountText == null || amountText.trim().isEmpty()) {
            errorMessage += "No payment amount entered!\n";
        } else {
            try {
                BigDecimal amount = new BigDecimal(amountText.trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    errorMessage += "Payment amount must be positive!\n";
                }
                // CRITICAL FIX: Removed validation against getBalanceDue()

            } catch (NumberFormatException e) {
                errorMessage += "Invalid amount format (must be a number).\n";
            }
        }

        if (paymentDatePicker.getValue() == null) {
            errorMessage += "No payment date selected!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.WARNING, "Invalid Fields", "Please correct the following errors:", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}