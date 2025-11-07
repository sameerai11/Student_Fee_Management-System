package model;

import javafx.beans.property.*;

public class Student {

    private final IntegerProperty studentId;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty department;
    private final StringProperty email;
    private final StringProperty phoneNumber;

    // NOTE: The 'user_id' field has been removed to match the database schema.

    // Full Constructor (Used when loading from DAO)
    public Student(int studentId, String firstName, String lastName, String department, String email, String phoneNumber) {
        this.studentId = new SimpleIntegerProperty(studentId);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.department = new SimpleStringProperty(department);
        this.email = new SimpleStringProperty(email);
        this.phoneNumber = new SimpleStringProperty(phoneNumber);
    }

    // Default Constructor (Used when creating a new student before saving)
    public Student() {
        this(0, "", "", "", "", "");
    }

    // --- Property Getters/Setters (Required for TableView binding) ---

    public IntegerProperty studentIdProperty() {
        return studentId;
    }

    public int getStudentId() {
        return studentId.get();
    }

    public void setStudentId(int studentId) {
        this.studentId.set(studentId);
    }

    // ---

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    // ---

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    // ---

    public StringProperty departmentProperty() {
        return department;
    }

    public String getDepartment() {
        return department.get();
    }

    public void setDepartment(String department) {
        this.department.set(department);
    }

    // ---

    public StringProperty emailProperty() {
        return email;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    // ---

    public StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber.get();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber.set(phoneNumber);
    }
}