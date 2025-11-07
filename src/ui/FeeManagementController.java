package ui;

import dao.StudentFeeDAO;
import dao.StudentDAO;
import dao.FeeTemplateDAO;
import model.FeeTemplate;
import model.StudentFee;
import model.Student;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Fee Assignment and Management UI.
 * FINAL VERSION: Fixed to match the 'fees' and 'studentfees' database tables and include working CRUD logic.
 */
public class FeeManagementController {

    // ----------------------------------------------------------------------
    // --- FXML FIELDS for Fee Structure Tab (Tab 1) ---
    // ----------------------------------------------------------------------
    @FXML private TableView<FeeTemplate> feeTable;
    @FXML private TableColumn<FeeTemplate, Integer> idColumn;
    @FXML private TableColumn<FeeTemplate, String> nameColumn;
    @FXML private TableColumn<FeeTemplate, BigDecimal> amountColumn;

    @FXML private TextField feeNameField;
    @FXML private TextField amountField;
    @FXML private Label statusLabel;

    // ----------------------------------------------------------------------
    // --- FXML FIELDS for Assigned Fees Tab (Tab 2) ---
    // ----------------------------------------------------------------------
    @FXML private TableView<StudentFee> assignedFeeTable;
    @FXML private TableColumn<StudentFee, Integer> studentFeeIdColumn;
    @FXML private TableColumn<StudentFee, String> studentNameColumn;
    @FXML private TableColumn<StudentFee, Integer> feeTypeColumn;
    @FXML private TableColumn<StudentFee, BigDecimal> feeAmountColumn;
    @FXML private TableColumn<StudentFee, Date> dueDateColumn;
    @FXML private TableColumn<StudentFee, String> statusColumn;
    @FXML private TableColumn<StudentFee, String> descriptionColumn;

    @FXML private TextField studentIdField; // Not used in this version's UI, but kept for future use
    @FXML private ComboBox<FeeTemplate> feeTemplateComboBox; // Not used in this version's UI, but kept for future use
    @FXML private TextField dueDateInput; // Not used in this version's UI, but kept for future use
    @FXML private TextField descriptionInput; // Not used in this version's UI, but kept for future use

    @FXML private TextField studentIdFilterInput;
    @FXML private Label statusMessageLabel;


    // --- DAO instances ---
    private final StudentFeeDAO studentFeeDAO = new StudentFeeDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final FeeTemplateDAO feeTemplateDAO = new FeeTemplateDAO();

    // --- Observable Lists ---
    private ObservableList<FeeTemplate> feeStructureData;


    /**
     * Initializes the controller and sets up column value factories.
     */
    @FXML
    public void initialize() {
        // --- Setup for Fee Structure Tab (Tab 1) ---
        idColumn.setCellValueFactory(new PropertyValueFactory<>("feeTemplateId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("defaultAmount"));

        loadFeeStructures();

        // --- Setup for Assigned Fees Tab (Tab 2) ---
        studentFeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentFeeId"));
        feeTypeColumn.setCellValueFactory(new PropertyValueFactory<>("feeId"));
        feeAmountColumn.setCellValueFactory(new PropertyValueFactory<>("feeAmount"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Custom cell value factory to fetch the Student Name
        studentNameColumn.setCellValueFactory(cellData -> {
            int studentId = cellData.getValue().getStudentId();
            try {
                // Fetch student details from the database
                Student student = studentDAO.getStudentById(studentId);
                if (student != null) {
                    return new SimpleStringProperty(student.getFirstName() + " " + student.getLastName());
                }
            } catch (SQLException e) {
                System.err.println("Database error fetching student name: " + e.getMessage());
            }
            return new SimpleStringProperty("[ID: " + studentId + "]");
        });

        // Load initial data for Assigned Fees table
        loadAllFees();

        // Populate Fee Template ComboBox (Used in the FeeAssignmentDialogController)
        if (feeStructureData != null) {
            feeTemplateComboBox.setItems(feeStructureData);
            feeTemplateComboBox.setCellFactory(lv -> new ListCell<FeeTemplate>() {
                @Override
                protected void updateItem(FeeTemplate item, boolean empty) {
                    super.updateItem(item, empty);
                    // Displaying name and amount from the simplified model
                    setText(empty ? null : item.getName() + " (₹" + item.getDefaultAmount() + ")");
                }
            });
            feeTemplateComboBox.setButtonCell(feeTemplateComboBox.getCellFactory().call(null));
        }
    }

    // ----------------------------------------------------------------------
    // --- FEE STRUCTURES TAB (Tab 1) METHODS ---
    // ----------------------------------------------------------------------

    /**
     * Loads ALL fee structure records into the FeeTable and updates the ComboBox list.
     */
    private void loadFeeStructures() {
        try {
            List<FeeTemplate> fees = feeTemplateDAO.getAllFees();
            feeStructureData = FXCollections.observableArrayList(fees);
            feeTable.setItems(feeStructureData);
            statusLabel.setText("Fee structures loaded successfully (" + fees.size() + " types).");

            if (feeTemplateComboBox != null) {
                feeTemplateComboBox.setItems(feeStructureData);
            }
        } catch (SQLException e) {
            statusLabel.setText("❌ Database Error loading fee structures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles adding a new fee structure.
     */
    @FXML
    private void handleAddFee() {
        String name = feeNameField.getText().trim();
        String amountText = amountField.getText().trim();

        if (name.isEmpty() || amountText.isEmpty()) {
            statusLabel.setText("Fee Name and Amount cannot be empty.");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            // Validation: Amount must be positive
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                statusLabel.setText("Amount must be a positive number.");
                return;
            }

            // Using the corrected constructor for a new fee
            FeeTemplate newFee = new FeeTemplate(name, amount);

            int newId = feeTemplateDAO.addFee(newFee);
            if (newId > 0) {
                statusLabel.setText("✅ Fee structure '" + name + "' added with ID: " + newId);
                feeNameField.clear();
                amountField.clear();
                loadFeeStructures(); // Refresh the table and ComboBox
            } else {
                statusLabel.setText("❌ Failed to add fee structure.");
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount format. Please enter a valid number.");
        } catch (SQLException e) {
            statusLabel.setText("❌ Database Error adding fee: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles editing the selected fee structure.
     */
    @FXML
    private void handleEditFee(ActionEvent event) {
        FeeTemplate selectedFee = feeTable.getSelectionModel().getSelectedItem();
        if (selectedFee == null) {
            statusLabel.setText("Please select a fee structure to edit.");
            return;
        }

        // --- 1. Use a simple TextInputDialog to get the new amount ---
        TextInputDialog dialog = new TextInputDialog(selectedFee.getDefaultAmount().toString());
        dialog.setTitle("Edit Fee Amount");
        dialog.setHeaderText("Updating Amount for: " + selectedFee.getName());
        dialog.setContentText("Enter New Amount:");

        Optional<String> result = dialog.showAndWait();

        // --- 2. Process the result and update ---
        result.ifPresent(newAmountText -> {
            try {
                BigDecimal newAmount = new BigDecimal(newAmountText.trim());

                // Validation: Amount must be positive
                if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new NumberFormatException("Amount must be positive.");
                }

                // Update the model object
                selectedFee.setDefaultAmount(newAmount);

                // 3. Call the DAO method
                if (feeTemplateDAO.updateFee(selectedFee)) {
                    statusLabel.setText("✅ Fee amount updated successfully for '" + selectedFee.getName() + "'.");
                    loadFeeStructures(); // 4. Refresh the table
                } else {
                    statusLabel.setText("❌ Failed to update fee structure. (No changes made or record not found).");
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("❌ Invalid amount format. Please enter a valid positive number.");
            } catch (SQLException e) {
                statusLabel.setText("❌ Database Error updating fee: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                statusLabel.setText("❌ An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Handles deleting the selected fee structure.
     */
    @FXML
    private void handleDeleteFee(ActionEvent event) {
        FeeTemplate selectedFee = feeTable.getSelectionModel().getSelectedItem();

        if (selectedFee == null) {
            statusLabel.setText("Please select a fee structure to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Fee Structure?");
        confirmation.setContentText("Are you sure you want to delete the fee structure: " + selectedFee.getName() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // The DAO is expected to throw IllegalStateException on Foreign Key violation
                if (feeTemplateDAO.deleteFee(selectedFee.getFeeTemplateId())) {
                    statusLabel.setText("✅ Fee structure deleted successfully.");
                    loadFeeStructures();
                } else {
                    // This block is only reached if executeUpdate() returned 0, not for FK errors
                    statusLabel.setText("❌ Failed to delete fee structure. Record not found.");
                }
            } catch (IllegalStateException e) {
                // Catch the custom exception thrown by DAO for Foreign Key violations
                statusLabel.setText("❌ Cannot delete fee: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Cannot delete fee structure.", e.getMessage() + "\n\nClear all assigned fees first.");
            } catch (SQLException e) {
                // Catch general SQL errors
                statusLabel.setText("❌ Database Error deleting fee: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ----------------------------------------------------------------------
    // --- ASSIGNED FEES TAB (Tab 2) METHODS ---
    // ----------------------------------------------------------------------

    /**
     * Loads ALL student fee records into the TableView.
     */
    @FXML
    private void loadAllFees() {
        try {
            List<StudentFee> fees = studentFeeDAO.getAllStudentFees();
            assignedFeeTable.setItems(FXCollections.observableArrayList(fees));
            statusMessageLabel.setText("All assigned fees loaded (" + fees.size() + " records).");
            studentIdFilterInput.clear();
        } catch (SQLException e) {
            statusMessageLabel.setText("❌ Database Error loading fees: " + e.getMessage());
        }
    }

    /**
     * Filters the TableView content to show only fees for a specific Student ID.
     */
    @FXML
    private void handleFilterByStudentId() {
        String input = studentIdFilterInput.getText().trim();
        if (input.isEmpty()) {
            loadAllFees();
            return;
        }

        try {
            int studentId = Integer.parseInt(input);
            List<StudentFee> fees = studentFeeDAO.getStudentFeesByStudentId(studentId);
            assignedFeeTable.setItems(FXCollections.observableArrayList(fees));
            statusMessageLabel.setText("Filtered fees loaded for Student ID: " + studentId + " (" + fees.size() + " records).");
        } catch (NumberFormatException e) {
            statusMessageLabel.setText("Invalid Student ID format. Please enter a number.");
        } catch (SQLException e) {
            statusMessageLabel.setText("❌ Database Error fetching filtered fees: " + e.getMessage());
        }
    }

    /**
     * Handles opening the dialog to assign a new fee to a student.
     * This relies on the FeeAssignmentDialogController.java file.
     */
    @FXML
    private void handleAssignFee(ActionEvent event) {
        try {
            // Load the FXML for the assignment dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FeeAssignmentDialogUI.fxml"));
            Parent root = loader.load();

            // Get the dialog controller
            FeeAssignmentDialogController controller = loader.getController();

            // Setup and show the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(assignedFeeTable.getScene().getWindow());
            dialogStage.setTitle("Assign New Student Fee");

            // Pass the stage to the controller (and any other required setup)
            controller.setDialogStage(dialogStage);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            // Check if OK was clicked before refreshing
            if (controller.isOkClicked()) {
                loadAllFees(); // Refresh after dialog closes
            }

        } catch (Exception e) {
            statusMessageLabel.setText("Error loading Fee Assignment Dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * HANDLER TO RECORD A NEW PAYMENT FOR THE SELECTED FEE.
     * FIX: Correctly instantiates and passes data to the FeePaymentDialogController.
     */
    @FXML
    private void handleRecordPayment() {
        StudentFee selectedFee = assignedFeeTable.getSelectionModel().getSelectedItem();

        if (selectedFee == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student fee record in the table to record a payment.");
            return;
        }

        // Check if status is PAID
        if ("PAID".equals(selectedFee.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Already Paid", "Fee is already fully paid according to status.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FeePaymentDialog.fxml"));
            Parent root = loader.load();

            // 1. Get the controller
            FeePaymentDialogController controller = loader.getController();

            // 2. Setup and show the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(assignedFeeTable.getScene().getWindow());
            dialogStage.setTitle("Record Payment for Fee ID: " + selectedFee.getStudentFeeId());

            // 3. Instantiate a new FeePayment object and link it to the selected fee
            model.FeePayment newPayment = new model.FeePayment();
            newPayment.setStudentFeeId(selectedFee.getStudentFeeId());

            // 4. Pass data to the dialog controller
            controller.setDialogStage(dialogStage);
            controller.setStudentFeeDetails(selectedFee);
            controller.setFeePayment(newPayment);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            // 5. Check if the payment was saved before refreshing
            if (controller.isSaved()) {
                statusMessageLabel.setText("✅ Payment recorded successfully. Refreshing table...");
                loadAllFees();
            } else {
                statusMessageLabel.setText("Payment canceled or failed to save. Table not refreshed.");
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Could not load payment dialog.", "Details: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred during payment processing.", "Details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}