package ui;

import dao.StudentDAO;
import model.Student;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.sql.SQLException;

public class StudentDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField departmentField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;

    private Stage dialogStage;
    private Student student;
    private boolean isEditMode;
    private boolean isSaved = false;
    private StudentDAO studentDAO = new StudentDAO();

    // Must be public for StudentManagementController to call it
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Must be public for StudentManagementController to call it
    public void setStudent(Student student, boolean isEditMode) {
        this.student = student;
        this.isEditMode = isEditMode;

        if (isEditMode) {
            dialogStage.setTitle("Edit Student Record (ID: " + student.getStudentId() + ")");
            // Populate fields for editing
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            departmentField.setText(student.getDepartment());
            emailField.setText(student.getEmail());
            phoneNumberField.setText(student.getPhoneNumber());
        } else {
            dialogStage.setTitle("Add New Student Record");
        }
    }

    // Must be public for StudentManagementController to call it
    public boolean isSaved() {
        return isSaved;
    }

    /**
     * Validates the user input in the text fields.
     */
    public boolean isInputValid() {
        String errorMessage = "";

        // --- Required Field Checks ---
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage += "No valid first name!\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage += "No valid last name!\n";
        }
        if (departmentField.getText() == null || departmentField.getText().trim().isEmpty()) {
            errorMessage += "No valid department!\n";
        }
        if (phoneNumberField.getText() == null || phoneNumberField.getText().trim().isEmpty()) {
            // FIX: Added validation for the phone number field
            errorMessage += "No valid phone number!\n";
        }

        // --- Format Checks ---
        String email = emailField.getText();
        if (email == null || !email.contains("@") || !email.contains(".")) {
            errorMessage += "No valid email address!\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (isInputValid()) {
            try {
                // Update the model object with the new field values (trimming whitespace)
                student.setFirstName(firstNameField.getText().trim());
                student.setLastName(lastNameField.getText().trim());
                student.setDepartment(departmentField.getText().trim());
                student.setEmail(emailField.getText().trim());
                student.setPhoneNumber(phoneNumberField.getText().trim());

                boolean success;

                if (isEditMode) {
                    // Update existing record
                    success = studentDAO.updateStudent(student);
                } else {
                    // Add new record and get the generated ID back to the student object
                    int newId = studentDAO.addStudent(student);
                    success = (newId != -1);
                }

                if (success) {
                    isSaved = true;
                    dialogStage.close();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Database operation failed. Record was not saved.");
                    alert.showAndWait();
                }

            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
}