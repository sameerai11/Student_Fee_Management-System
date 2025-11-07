package ui;

import dao.StudentDAO;
import model.Student;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
// import java.sql.Date; // Not needed since DateOfBirth is removed
// import java.time.LocalDate; // Not needed
// import java.time.format.DateTimeParseException; // Not needed

public class StudentManagementUI {

    private final Scanner scanner;
    private final StudentDAO studentDAO;

    public StudentManagementUI(Scanner scanner) {
        this.scanner = scanner;
        this.studentDAO = new StudentDAO();
    }

    public void show() {
        System.out.println("\n--- STUDENT MANAGEMENT ---");
        boolean running = true;

        while (running) {
            System.out.println("\nSelect Action:");
            System.out.println("1. Add New Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student Details");
            System.out.println("4. Back to Admin Dashboard");
            System.out.print("Enter choice (1-4): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        addStudent();
                        break;
                    case 2:
                        viewAllStudents();
                        break;
                    case 3:
                        updateStudent();
                        break;
                    case 4:
                        running = false;
                        System.out.println("<- Returning to Admin Dashboard...");
                        break;
                    default:
                        System.err.println("Invalid choice. Please enter 1, 2, 3, or 4.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter a number.");
            } catch (SQLException e) {
                System.err.println("Database Error: " + e.getMessage());
            }
        }
    }

    private void addStudent() throws SQLException {
        System.out.println("\n--- ADD NEW STUDENT ---");
        // We initialize a new Student object. userId will be set later.
        Student student = new Student();

        System.out.print("Enter First Name: ");
        student.setFirstName(scanner.nextLine().trim());

        System.out.print("Enter Last Name: ");
        student.setLastName(scanner.nextLine().trim());

        // --- REMOVED FIELD: Enrollment Number ---
        // System.out.print("Enter Enrollment Number: ");
        // student.setEnrollmentNumber(scanner.nextLine().trim());

        System.out.print("Enter Department: ");
        student.setDepartment(scanner.nextLine().trim());

        System.out.print("Enter Email (optional): ");
        student.setEmail(scanner.nextLine().trim());

        System.out.print("Enter Phone Number (optional): ");
        student.setPhoneNumber(scanner.nextLine().trim());

        // --- REMOVED FIELD: Date of Birth ---
        // System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
        // String dobInput = scanner.nextLine().trim();
        // try {
        //     if (!dobInput.isEmpty()) {
        //         student.setDateOfBirth(Date.valueOf(LocalDate.parse(dobInput)));
        //     }
        // } catch (DateTimeParseException e) {
        //     System.err.println("Invalid date format. Using default (NULL). Please use YYYY-MM-DD.");
        // }

        // Note: userId is usually handled during User creation and linking. Leaving as default (0 or null in the model).

        int newId = studentDAO.addStudent(student);

        if (newId != -1) {
            System.out.println("✅ Student added successfully! ID: " + newId);
        } else {
            System.err.println("❌ Failed to add student. Check for duplicate email/phone or database constraints.");
        }
    }

    private void viewAllStudents() throws SQLException {
        System.out.println("\n--- ALL REGISTERED STUDENTS ---");
        List<Student> students = studentDAO.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No students registered yet.");
            return;
        }

        // Print header - REMOVED ENROLLMENT NUMBER
        System.out.printf("%-5s | %-15s | %-15s | %-15s | %-25s | %-15s%n",
                "ID", "First Name", "Last Name", "Department", "Email", "Phone");
        System.out.println("--------------------------------------------------------------------------------------------------");

        // Print student details
        for (Student s : students) {
            System.out.printf("%-5d | %-15s | %-15s | %-15s | %-25s | %-15s%n",
                    s.getStudentId(),
                    // REMOVED ENROLLMENT NUMBER column, promoting First Name to 2nd position
                    s.getFirstName(),
                    s.getLastName(),
                    s.getDepartment(),
                    s.getEmail(),
                    s.getPhoneNumber());
        }
    }

    private void updateStudent() throws SQLException {
        System.out.println("\n--- UPDATE STUDENT DETAILS ---");

        System.out.print("Enter Student ID to update: ");
        if (!scanner.hasNextInt()) {
            System.err.println("Invalid input. Must be a number.");
            scanner.nextLine();
            return;
        }
        int idToUpdate = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        Student existingStudent = studentDAO.getStudentById(idToUpdate);

        if (existingStudent == null) {
            System.err.println("❌ Student with ID " + idToUpdate + " not found.");
            return;
        }

        System.out.println("Current Details: " + existingStudent.getFirstName() + " " + existingStudent.getLastName() +
                // REMOVED Enrollment Number from current details display
                ", Dept: " + existingStudent.getDepartment());
        System.out.println("Enter new details (leave blank to keep current value):");

        // First Name
        System.out.print("New First Name [" + existingStudent.getFirstName() + "]: ");
        String newFirstName = scanner.nextLine().trim();
        if (!newFirstName.isEmpty()) {
            existingStudent.setFirstName(newFirstName);
        }

        // Last Name
        System.out.print("New Last Name [" + existingStudent.getLastName() + "]: ");
        String newLastName = scanner.nextLine().trim();
        if (!newLastName.isEmpty()) {
            existingStudent.setLastName(newLastName);
        }

        // Enrollment Number - REMOVED
        // System.out.print("New Enrollment Number [" + existingStudent.getEnrollmentNumber() + "]: ");
        // String newEnrollmentNumber = scanner.nextLine().trim();
        // if (!newEnrollmentNumber.isEmpty()) {
        //     existingStudent.setEnrollmentNumber(newEnrollmentNumber);
        // }

        // Department
        System.out.print("New Department [" + existingStudent.getDepartment() + "]: ");
        String newDepartment = scanner.nextLine().trim();
        if (!newDepartment.isEmpty()) {
            existingStudent.setDepartment(newDepartment);
        }

        // Email
        System.out.print("New Email [" + existingStudent.getEmail() + "]: ");
        String newEmail = scanner.nextLine().trim();
        if (!newEmail.isEmpty()) {
            existingStudent.setEmail(newEmail);
        }

        // Phone Number
        System.out.print("New Phone Number [" + existingStudent.getPhoneNumber() + "]: ");
        String newPhone = scanner.nextLine().trim();
        if (!newPhone.isEmpty()) {
            existingStudent.setPhoneNumber(newPhone);
        }

        // Date of Birth - REMOVED
        // String currentDob = existingStudent.getDateOfBirth() != null ? existingStudent.getDateOfBirth().toString() : "NULL";
        // System.out.print("New Date of Birth (YYYY-MM-DD) [" + currentDob + "]: ");
        // String newDobInput = scanner.nextLine().trim();
        // if (!newDobInput.isEmpty()) {
        //     try {
        //         existingStudent.setDateOfBirth(Date.valueOf(LocalDate.parse(newDobInput)));
        //     } catch (DateTimeParseException e) {
        //         System.err.println("Invalid date format. DOB was not updated. Please use YYYY-MM-DD.");
        //     }
        // }
        // Note: userId is not managed here.

        if (studentDAO.updateStudent(existingStudent)) {
            System.out.println("✅ Student ID " + idToUpdate + " updated successfully!");
        } else {
            System.err.println("❌ Failed to update student.");
        }
    }
}