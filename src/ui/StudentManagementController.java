package ui;
import dao.StudentDAO;
import model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.beans.property.SimpleStringProperty; // Needed for the name column binding
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class StudentManagementController {

    private final StudentDAO studentDAO = new StudentDAO(); // DAO instance for all methods
    private ObservableList<Student> students = FXCollections.observableArrayList();

    @FXML private Label statusLabel;

    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Integer> idColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> courseColumn;
    @FXML private TableColumn<Student, String> emailColumn;
    @FXML private TableColumn<Student, Double> balanceColumn;

    @FXML
    public void initialize() {
        // --- Column Binding Setup ---
        idColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        // NOTE: Replacing the complex Bindings with a SimpleStringProperty if Student model uses simple Strings
        nameColumn.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            // Assuming getFirstName() and getLastName() return simple Strings
            String fullName = student.getFirstName() + " " + student.getLastName();
            return new SimpleStringProperty(fullName);
        });

        courseColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        // balanceColumn will require a FeeDAO implementation later

        // Set the ObservableList to the TableView once
        studentTable.setItems(students);

        // --- Initial Data Load ---
        loadStudentData();
    }

    /**
     * Public method to load/reload all student data from the database.
     */
    public void loadStudentData() {
        try {
            List<Student> studentList = studentDAO.getAllStudents();

            // Clear existing data and add all new data.
            students.clear();
            students.addAll(studentList);

            statusLabel.setText("Successfully loaded " + students.size() + " student records.");
        } catch (SQLException e) {
            statusLabel.setText("Error loading student data from database.");
            e.printStackTrace();
        }
    }

    // Generic method to launch the student dialog for Add or Edit
    private void showStudentEditDialog(Student student, boolean isEditMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StudentDialogUI.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(studentTable.getScene().getWindow());

            StudentDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setStudent(student, isEditMode); // Pass the student object to the dialog

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            // --- CRITICAL REFRESH LOGIC ---
            if (controller.isSaved()) {
                if (!isEditMode) {
                    // For a NEW student, the DAO added the student and set the new ID.
                    // Now, we simply add the same *student object* to the ObservableList.
                    students.add(student);
                    statusLabel.setText("✅ New student record saved and added to table.");
                } else {
                    // For an EDITED student, the object in the list is the same one passed to the dialog.
                    // We call refresh() to ensure the TableView updates immediately.
                    studentTable.refresh();
                    statusLabel.setText("✅ Student record updated successfully.");
                }
            }
        } catch (IOException e) {
            statusLabel.setText("❌ Error loading student dialog form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Action Handlers ---

    @FXML
    private void handleAddStudent(ActionEvent event) {
        showStudentEditDialog(new Student(), false);
    }

    @FXML
    private void handleEditStudent(ActionEvent event) {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();

        if (selectedStudent != null) {
            showStudentEditDialog(selectedStudent, true);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a student from the table to edit.");
            alert.showAndWait();
        }
    }

    /**
     * Handles deleting the selected student record.
     * **FIXED to handle IllegalStateException thrown by DAO on Foreign Key violations.**
     */
    @FXML
    private void handleDeleteStudent(ActionEvent event) {
        Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();

        if (selectedStudent != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Student Record?");
            confirmation.setContentText("Are you sure you want to permanently delete the record for " +
                    selectedStudent.getFirstName() + " " + selectedStudent.getLastName() + "?\n\nThis action cannot be undone.");

            Optional<ButtonType> result = confirmation.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // 1. Call the DAO method (which throws IllegalStateException on FK violation)
                    if (studentDAO.deleteStudent(selectedStudent.getStudentId())) {
                        studentTable.getItems().remove(selectedStudent);
                        statusLabel.setText("✅ Student " + selectedStudent.getStudentId() + " deleted successfully.");
                    } else {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to delete student from the database. Record not found.");
                        errorAlert.showAndWait();
                    }
                } catch (IllegalStateException e) {
                    // 2. CATCH THE FOREIGN KEY DEPENDENCY EXCEPTION
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Deletion Failed: Dependency Found");
                    errorAlert.setHeaderText("Cannot delete student.");
                    errorAlert.setContentText(e.getMessage() + "\n\nPlease clear all assigned fees for this student first.");
                    errorAlert.showAndWait();
                    statusLabel.setText("❌ Deletion Failed: Student has outstanding fee obligations.");
                } catch (SQLException e) {
                    // 3. Catch General Database Errors
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Database Error during deletion.\nDetails: " + e.getMessage());
                    errorAlert.showAndWait();
                    e.printStackTrace();
                    statusLabel.setText("❌ Database Error during deletion.");
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a student from the table to delete.");
            alert.showAndWait();
        }
    }
}