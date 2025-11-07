package ui;

import dao.StudentFeeDAO;
import dao.FeePaymentDAO; // CORRECTED: Using FeePaymentDAO
import model.StudentFee;
import model.FeePayment; // CORRECTED: Using FeePayment model
import model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate; // Use LocalDate for consistency
import java.util.ArrayList;
import java.util.List;

public class StudentController {

    // --- Student Fee Table FXML Bindings ---
    @FXML private TableView<StudentFee> studentFeeTable;
    @FXML private TableColumn<StudentFee, Integer> sfIdColumn;
    @FXML private TableColumn<StudentFee, Integer> sfFeeTypeColumn;
    @FXML private TableColumn<StudentFee, BigDecimal> sfAmountColumn;
    @FXML private TableColumn<StudentFee, Date> sfDueDateColumn;
    @FXML private TableColumn<StudentFee, String> sfStatusColumn;
    @FXML private TableColumn<StudentFee, String> sfDescriptionColumn;

    // --- Transaction Table FXML Bindings (now FeePayment) ---
    @FXML private TableView<FeePayment> transactionTable; // CORRECTED: TableView type
    @FXML private TableColumn<FeePayment, Integer> trxIdColumn;
    @FXML private TableColumn<FeePayment, Integer> trxFeeIdColumn;
    @FXML private TableColumn<FeePayment, BigDecimal> trxAmountColumn;
    @FXML private TableColumn<FeePayment, LocalDate> trxDateColumn; // Using LocalDate/Date
    @FXML private TableColumn<FeePayment, String> trxMethodColumn;

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // DAO Instances
    private final StudentFeeDAO studentFeeDAO = new StudentFeeDAO();
    private final FeePaymentDAO feePaymentDAO = new FeePaymentDAO(); // CORRECTED: Using FeePaymentDAO

    private User loggedInUser;
    private int studentId;

    /**
     * Called by the LoginController after a successful student login.
     * This is the critical step that passes the User object.
     */
    public void initData(User user) {
        this.loggedInUser = user;
        // Use the studentId property we added to the User model earlier
        this.studentId = user.getStudentId() != null ? user.getStudentId() : -1;

        if (this.studentId == -1) {
            welcomeLabel.setText("❌ Error: User Not Linked to Student ID");
            statusLabel.setText("Please contact administration to link your account.");
        } else {
            // Display username and ID
            welcomeLabel.setText("Welcome, " + user.getUsername() + " (ID: " + this.studentId + ")");
            // Proceed to set up columns and load data
            setupTableColumns();
            loadStudentData();
        }
    }

    // Set up table columns using PropertyValueFactory
    private void setupTableColumns() {
        // --- Setup Student Fee Table Columns ---
        sfIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentFeeId"));
        sfFeeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("feeId"));
        sfAmountColumn.setCellValueFactory(new PropertyValueFactory<>("feeAmount"));
        sfDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        sfStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        sfDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // --- Setup FeePayment Table Columns ---
        // Names must match fields in the FeePayment model:
        trxIdColumn.setCellValueFactory(new PropertyValueFactory<>("paymentId")); // CORRECTED: paymentId
        trxFeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentFeeId"));
        trxAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount")); // CORRECTED: amount
        trxDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate")); // CORRECTED: paymentDate (LocalDate)
        trxMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
    }

    // Main data loading function (also used by the refresh button)
    @FXML
    private void loadStudentData() {
        if (this.studentId == -1) return;

        try {
            // 1. Load Assigned Fees
            List<StudentFee> assignedFees = studentFeeDAO.getStudentFeesByStudentId(this.studentId);
            studentFeeTable.setItems(FXCollections.observableArrayList(assignedFees));

            // 2. Load Transactions (FeePayments) related to those fees
            List<FeePayment> allPayments = new ArrayList<>();
            for (StudentFee sf : assignedFees) {
                // CORRECTED: Use the FeePaymentDAO method name
                allPayments.addAll(feePaymentDAO.getPaymentsByStudentFeeId(sf.getStudentFeeId()));
            }
            transactionTable.setItems(FXCollections.observableArrayList(allPayments));

            statusLabel.setText("✅ Fees and transactions loaded successfully. Showing " +
                    assignedFees.size() + " fees and " + allPayments.size() + " payments.");

        } catch (SQLException e) {
            statusLabel.setText("❌ Database Error: Could not load your fee data.");
            e.printStackTrace();
        }
    }
}