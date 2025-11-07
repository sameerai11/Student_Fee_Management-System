package ui;

import dao.StudentFeeDAO; // CRITICAL: Import the correct DAO
import model.StudentFee; // CRITICAL: Import the correct model for assignment
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class FeeManagementUI {

    private final Scanner scanner;
    private final StudentFeeDAO studentFeeDAO; // CRITICAL: Use StudentFeeDAO for assignment/status

    public FeeManagementUI(Scanner scanner) {
        this.scanner = scanner;
        this.studentFeeDAO = new StudentFeeDAO(); // Initialize the correct DAO
    }

    public void show() {
        System.out.println("\n--- FEE MANAGEMENT (ASSIGNMENT & STATUS) ---");
        boolean running = true;

        while (running) {
            System.out.println("\nSelect Fee Action:");
            System.out.println("1. Assign New Fee to Student");
            System.out.println("2. View All Assigned Fees by Student ID"); // Changed wording for clarity
            System.out.println("3. Update Fee Status (Manually)"); // Clarified intent
            System.out.println("4. Back to Admin Dashboard");
            System.out.print("Enter choice (1-4): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        assignFee();
                        break;
                    case 2:
                        viewFeesByStudent();
                        break;
                    case 3:
                        updateStatus();
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
                e.printStackTrace(); // Added for debugging specific DB issues
            }
        }
    }

    private void assignFee() throws SQLException {
        System.out.println("\n--- ASSIGN NEW FEE ---");

        // CRITICAL FIX: Use StudentFee model, not Fee model
        StudentFee studentFee = new StudentFee();

        System.out.print("Enter Student ID to assign fee: ");
        if (!scanner.hasNextInt()) {
            System.err.println("Invalid input. Must be a number.");
            scanner.nextLine();
            return;
        }
        int studentId = scanner.nextInt();
        scanner.nextLine();
        studentFee.setStudentId(studentId);

        System.out.print("Enter Fee Amount (e.g., 550.00): ");
        try {
            BigDecimal amount = new BigDecimal(scanner.nextLine());
            studentFee.setFeeAmount(amount);
        } catch (NumberFormatException e) {
            System.err.println("Invalid amount format. Assignment cancelled.");
            return;
        }

        System.out.print("Enter Due Date (YYYY-MM-DD, or leave blank for 30 days): ");
        String dateInput = scanner.nextLine().trim();
        Date dueDate;

        if (dateInput.isEmpty()) {
            dueDate = Date.valueOf(LocalDate.now().plusDays(30));
            System.out.println("Default Due Date set to: " + dueDate);
        } else {
            try {
                dueDate = Date.valueOf(dateInput);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid date format. Please use YYYY-MM-DD. Assignment cancelled.");
                return;
            }
        }
        studentFee.setDueDate(dueDate);

        System.out.print("Enter Fee Description (e.g., Tuition Fee - Fall 2025): ");
        studentFee.setDescription(scanner.nextLine().trim());
        studentFee.setStatus("OUTSTANDING"); // Initialize status explicitly

        int newId = studentFeeDAO.assignFee(studentFee);

        if (newId != -1) {
            System.out.println("✅ Fee assigned successfully! StudentFee ID: " + newId + " to Student ID: " + studentId);
        } else {
            System.err.println("❌ Failed to assign fee. Check if Student ID exists.");
        }
    }

    private void viewFeesByStudent() throws SQLException {
        System.out.println("\n--- VIEW ASSIGNED FEES BY STUDENT ID ---");
        System.out.print("Enter Student ID to view fees: ");
        if (!scanner.hasNextInt()) {
            System.err.println("Invalid input. Must be a number.");
            scanner.nextLine();
            return;
        }
        int studentId = scanner.nextInt();
        scanner.nextLine();

        List<StudentFee> studentFees = studentFeeDAO.getStudentFeesByStudentId(studentId);

        if (studentFees.isEmpty()) {
            System.out.println("No fees have been assigned to Student ID " + studentId + ".");
            return;
        }

        // Print header
        System.out.printf("%-5s | %-12s | %-12s | %-12s | %-10s | %-40s%n",
                "SF ID", "FEE ID", "AMOUNT", "DUE DATE", "STATUS", "DESCRIPTION");
        System.out.println("--------------------------------------------------------------------------------------------------");

        // Print fee details
        for (StudentFee sf : studentFees) {
            System.out.printf("%-5d | %-12d | $%-10s | %-12s | %-10s | %-40s%n",
                    sf.getStudentFeeId(),
                    sf.getFeeId(),
                    sf.getFeeAmount(),
                    sf.getDueDate().toString(),
                    sf.getStatus(),
                    sf.getDescription());
        }
    }

    private void updateStatus() throws SQLException {
        System.out.println("\n--- UPDATE FEE PAYMENT STATUS (MANUAL OVERRIDE) ---");

        System.out.print("Enter StudentFee ID to update status: ");
        if (!scanner.hasNextInt()) {
            System.err.println("Invalid input. Must be a number.");
            scanner.nextLine();
            return;
        }
        int studentFeeId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter new Status (PAID/OUTSTANDING/OVERDUE): ");
        String newStatus = scanner.nextLine().trim().toUpperCase();

        if (!newStatus.equals("PAID") && !newStatus.equals("OUTSTANDING") && !newStatus.equals("OVERDUE")) {
            System.err.println("Invalid status. Must be PAID, OUTSTANDING, or OVERDUE.");
            return;
        }

        // FIX: Use the new, non-deprecated manuallyUpdateStatus method
        if (studentFeeDAO.manuallyUpdateStatus(studentFeeId, newStatus)) {
            System.out.println("✅ StudentFee ID " + studentFeeId + " status updated to " + newStatus + " successfully!");
        } else {
            System.err.println("❌ Failed to update status. Check if StudentFee ID exists.");
        }
    }
}
