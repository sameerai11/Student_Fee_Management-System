package ui;

import dao.FeePaymentDAO; // CORRECTED: Using FeePaymentDAO
import dao.StudentFeeDAO;
import model.FeePayment; // CORRECTED: Using FeePayment model
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate; // Use LocalDate for consistency with the model
import java.time.LocalDateTime;
import java.util.List;

public class TransactionController {

    // --- Input Fields (for Manual Payment/Transaction Assignment) ---
    @FXML private TextField studentFeeIdField;
    @FXML private TextField amountPaidField;
    @FXML private TextField methodField;
    @FXML private Label statusLabel;

    // --- Table View components ---
    // CRITICAL FIX: TableView must use the FeePayment model
    @FXML private TableView<FeePayment> transactionTable;
    // CRITICAL FIX: Column types and PropertyValueFactory names must match FeePayment.java
    @FXML private TableColumn<FeePayment, Integer> trxIdColumn;
    @FXML private TableColumn<FeePayment, Integer> feeIdColumn;
    @FXML private TableColumn<FeePayment, BigDecimal> amountColumn;
    @FXML private TableColumn<FeePayment, LocalDate> dateColumn; // Using LocalDate from the model
    @FXML private TableColumn<FeePayment, String> methodColumn;


    private final FeePaymentDAO feePaymentDAO = new FeePaymentDAO(); // CORRECTED DAO
    private final StudentFeeDAO studentFeeDAO = new StudentFeeDAO();

    @FXML
    public void initialize() {
        System.out.println("Transaction Controller Initialized and refreshing data.");

        // 1. Initialize Table Columns
        // CRITICAL FIX: Use getters from FeePayment model
        trxIdColumn.setCellValueFactory(new PropertyValueFactory<>("paymentId"));       // Getter: getPaymentId()
        feeIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentFeeId"));  // Getter: getStudentFeeId()
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));         // Getter: getAmount()
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));    // Getter: getPaymentDate()
        methodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod")); // Getter: getPaymentMethod()

        // 2. Load Data
        loadAllTransactions();
    }

    private void loadAllTransactions() {
        try {
            // NOTE: We assume FeePaymentDAO has a getAllPayments() method for Admin view
            List<FeePayment> payments = feePaymentDAO.getAllPayments(); // Assuming this method exists
            ObservableList<FeePayment> observableList = FXCollections.observableArrayList(payments);
            transactionTable.setItems(observableList);
            statusLabel.setText("Transactions loaded: " + payments.size() + " records.");
        } catch (SQLException e) {
            statusLabel.setText("❌ Database Error loading all transactions. Check FeePaymentDAO.getAllPayments().");
            e.printStackTrace();
        }
    }


    @FXML
    private void handleRecordPayment(ActionEvent event) {
        String feeIdText = studentFeeIdField.getText().trim();
        String amountText = amountPaidField.getText().trim();
        String method = methodField.getText().trim();

        if (feeIdText.isEmpty() || amountText.isEmpty() || method.isEmpty()) {
            statusLabel.setText("❌ Error: All fields must be filled.");
            return;
        }

        try {
            int studentFeeId = Integer.parseInt(feeIdText);
            BigDecimal amount = new BigDecimal(amountText);

            // 1. Insert the FeePayment record first
            FeePayment newPayment = new FeePayment();
            newPayment.setStudentFeeId(studentFeeId);
            newPayment.setAmount(amount);
            newPayment.setPaymentMethod(method);
            newPayment.setPaymentDate(LocalDate.now()); // Set current date

            // CRITICAL FIX: Call the correct DAO method
            int paymentId = feePaymentDAO.addPayment(newPayment);

            if (paymentId != -1) {

                // 2. Update the Fee Status to PAID
                boolean feeStatusUpdated = studentFeeDAO.manuallyUpdateStatus(studentFeeId, "PAID");

                if (feeStatusUpdated) {
                    statusLabel.setText("✅ Payment recorded (ID: " + paymentId + "). Student Fee ID " + studentFeeId + " marked as PAID.");
                    // 3. Refresh the table view immediately
                    loadAllTransactions();
                    // Clear fields
                    studentFeeIdField.clear();
                    amountPaidField.clear();
                    methodField.clear();
                } else {
                    statusLabel.setText("⚠️ Transaction recorded, but FAILED to update Fee status to PAID for ID " + studentFeeId + ". Check console.");
                }
            } else {
                statusLabel.setText("❌ Failed to record transaction in FeePaymentDAO.");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("❌ Error: Fee ID and Amount must be valid numbers.");
        } catch (SQLException e) {
            statusLabel.setText("❌ Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}