package ui;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Controller for the Fee Reporting and Search UI (FeeReportUI.fxml).
 * Handles loading, displaying, and filtering student fee obligations.
 */
public class FeeReportController {

    // --- FXML UI Elements ---
    @FXML
    private TextField studentIdFilterField;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private TableView<FeeReportController.DisplayStudentFee> feeTableView;
    @FXML
    private Label resultCountLabel;

    // --- Table Columns ---
    @FXML private TableColumn<DisplayStudentFee, Integer> idCol;
    @FXML private TableColumn<DisplayStudentFee, Integer> studentIdCol;
    @FXML private TableColumn<DisplayStudentFee, String> feeTypeCol;
    @FXML private TableColumn<DisplayStudentFee, BigDecimal> amountCol;
    @FXML private TableColumn<DisplayStudentFee, BigDecimal> paidCol;
    @FXML private TableColumn<DisplayStudentFee, String> statusCol;
    @FXML private TableColumn<DisplayStudentFee, LocalDate> dueDateCol;
    @FXML private TableColumn<DisplayStudentFee, String> descriptionCol;


    // --- Data Management ---
    private ObservableList<DisplayStudentFee> masterFeeData;
    private FilteredList<DisplayStudentFee> filteredFeeData;

    /**
     * Initializes the controller class. Automatically called after FXML load.
     */
    @FXML
    private void initialize() {
        // 1. Setup Table Columns (Mapping model properties to columns)
        setupTableColumns();

        // 2. Load and set up Master Data (Mock Data Retrieval)
        loadMasterData();

        // 3. Setup Filtered List for TableView
        filteredFeeData = new FilteredList<>(masterFeeData, p -> true); // Initially show all data
        feeTableView.setItems(filteredFeeData);

        // 4. Setup Status ComboBox
        setupStatusComboBox();

        // 5. Setup dynamic listener for Student ID field (live filtering)
        studentIdFilterField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Apply filtering instantly whenever the Student ID text changes
            handleSearch();
        });

        // 6. Initial update of count label
        updateResultCountLabel();
    }

    /**
     * Configures the cell value factories for the TableView columns.
     */
    private void setupTableColumns() {
        idCol.setCellValueFactory(cellData -> cellData.getValue().id.asObject());
        studentIdCol.setCellValueFactory(cellData -> cellData.getValue().studentId.asObject());
        feeTypeCol.setCellValueFactory(cellData -> cellData.getValue().feeType);
        amountCol.setCellValueFactory(cellData -> cellData.getValue().feeAmount);
        paidCol.setCellValueFactory(cellData -> cellData.getValue().amountPaid);
        statusCol.setCellValueFactory(cellData -> cellData.getValue().status);

        // Special handling for Date to LocalDate conversion
        dueDateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().dueDate.get().toLocalDate()));
        descriptionCol.setCellValueFactory(cellData -> cellData.getValue().description);
    }

    /**
     * Sets up the status filter combo box with available statuses.
     */
    private void setupStatusComboBox() {
        List<String> statuses = Arrays.asList("Any Status", "PENDING", "OVERDUE", "CANCELED", "PAID");
        statusFilterComboBox.setItems(FXCollections.observableArrayList(statuses));
        statusFilterComboBox.setValue("Any Status"); // Set default

        // Add listener to trigger search when status selection changes
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleSearch());
    }

    /**
     * Simulates loading all fee data from the database (DAO).
     */
    private void loadMasterData() {
        // In a real application, this would call studentFeeDAO.getAllFees()
        masterFeeData = FXCollections.observableArrayList(
                new DisplayStudentFee(1, 1001, "Tuition", new BigDecimal("15000.00"), Date.valueOf(LocalDate.now().plusDays(30)), "PENDING", new BigDecimal("0.00"), "Fall 2024 Tuition"),
                new DisplayStudentFee(2, 1002, "Library Fee", new BigDecimal("500.00"), Date.valueOf(LocalDate.now().minusDays(10)), "OVERDUE", new BigDecimal("0.00"), "Late return fee"),
                new DisplayStudentFee(3, 1001, "Sports Fee", new BigDecimal("2500.00"), Date.valueOf(LocalDate.now().minusDays(60)), "PAID", new BigDecimal("2500.00"), "Gym membership"),
                new DisplayStudentFee(4, 1003, "Miscellaneous", new BigDecimal("120.50"), Date.valueOf(LocalDate.now().plusDays(5)), "PENDING", new BigDecimal("0.00"), "ID card replacement"),
                new DisplayStudentFee(5, 1002, "Tuition", new BigDecimal("15000.00"), Date.valueOf(LocalDate.now().plusMonths(2)), "PENDING", new BigDecimal("0.00"), "Spring 2025 Tuition"),
                new DisplayStudentFee(6, 1004, "Library Fee", new BigDecimal("750.00"), Date.valueOf(LocalDate.now().minusMonths(3)), "CANCELED", new BigDecimal("0.00"), "Administrative error")
        );
    }

    /**
     * Applies the filters based on current user input.
     */
    @FXML
    private void handleSearch() {
        String studentIdText = studentIdFilterField.getText() == null ? "" : studentIdFilterField.getText().trim();
        String selectedStatus = statusFilterComboBox.getValue();

        // 1. Define the combined predicate for filtering
        Predicate<DisplayStudentFee> studentIdPredicate = fee -> {
            // If the filter is empty, always pass (true)
            if (studentIdText.isEmpty()) {
                return true;
            }
            // Check if the student ID matches the filter text (case insensitive start match)
            return String.valueOf(fee.getStudentId()).startsWith(studentIdText);
        };

        Predicate<DisplayStudentFee> statusPredicate = fee -> {
            // If "Any Status" is selected, always pass (true)
            if (selectedStatus == null || selectedStatus.equals("Any Status")) {
                return true;
            }
            // Check if the status matches the selected status
            return fee.getStatus().equalsIgnoreCase(selectedStatus);
        };

        // Combine predicates using AND logic
        Predicate<DisplayStudentFee> combinedPredicate = studentIdPredicate.and(statusPredicate);

        // 2. Set the predicate on the FilteredList
        filteredFeeData.setPredicate(combinedPredicate);

        // 3. Update the result count
        updateResultCountLabel();
    }

    /**
     * Resets all filter fields and refreshes the search.
     */
    @FXML
    private void handleReset() {
        studentIdFilterField.setText("");
        statusFilterComboBox.setValue("Any Status");
        // handleSearch() will be called automatically due to listeners
    }

    /**
     * Updates the label showing the number of records currently visible.
     */
    private void updateResultCountLabel() {
        int count = filteredFeeData.size();
        resultCountLabel.setText("Displaying " + count + " records.");
    }

    // --- Model Class for TableView Display ---
    // Note: We use JavaFX properties for seamless integration with TableView
    public static class DisplayStudentFee {
        private final SimpleIntegerProperty id;
        private final SimpleIntegerProperty studentId;
        private final SimpleStringProperty feeType;
        private final SimpleObjectProperty<BigDecimal> feeAmount;
        private final SimpleObjectProperty<Date> dueDate;
        private final SimpleStringProperty status;
        private final SimpleObjectProperty<BigDecimal> amountPaid;
        private final SimpleStringProperty description;

        public DisplayStudentFee(int id, int studentId, String feeType, BigDecimal feeAmount, Date dueDate, String status, BigDecimal amountPaid, String description) {
            this.id = new SimpleIntegerProperty(id);
            this.studentId = new SimpleIntegerProperty(studentId);
            this.feeType = new SimpleStringProperty(feeType);
            this.feeAmount = new SimpleObjectProperty<>(feeAmount);
            this.dueDate = new SimpleObjectProperty<>(dueDate);
            this.status = new SimpleStringProperty(status);
            this.amountPaid = new SimpleObjectProperty<>(amountPaid);
            this.description = new SimpleStringProperty(description);
        }

        // Getters (used implicitly by TableView via CellValueFactory)
        public int getStudentId() { return studentId.get(); }
        public String getStatus() { return status.get(); }
        // The remaining getters for properties are required for TableView to function correctly:
        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleIntegerProperty studentIdProperty() { return studentId; }
        public SimpleStringProperty feeTypeProperty() { return feeType; }
        public SimpleObjectProperty<BigDecimal> feeAmountProperty() { return feeAmount; }
        public SimpleObjectProperty<Date> dueDateProperty() { return dueDate; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleObjectProperty<BigDecimal> amountPaidProperty() { return amountPaid; }
        public SimpleStringProperty descriptionProperty() { return description; }
    }
}
