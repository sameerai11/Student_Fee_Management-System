package ui;
import model.StudentFee;
import dao.TransactionDAO;
import dao.StudentFeeDAO;
import model.User;
import model.Transaction;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class StudentUI {

    private final User studentUser;
    private final Scanner scanner;
    private final StudentFeeDAO studentFeeDAO;
    private final TransactionDAO transactionDAO;

    public StudentUI(User studentUser) {
        this.studentUser = studentUser;
        this.scanner = new Scanner(System.in);
        this.studentFeeDAO = new StudentFeeDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public void show() {
        System.out.println("\n--- STUDENT PORTAL ---");
        boolean running = true;

        // Ensure the user is actually linked to a student record
        if (studentUser.getStudentId() == null) {
            System.err.println("❌ Error: Your user account is not linked to a student ID. Please contact administration.");
            return; // Exit the UI immediately
        }

        int studentId = studentUser.getStudentId();


        while (running) {
            System.out.println("\n-----------------------------------------");
            System.out.println("  Welcome, Student (ID: " + studentId + ")");
            System.out.println("-----------------------------------------");
            System.out.println("1. View My Fee Status");
            System.out.println("2. View My Transaction History");
            System.out.println("3. Logout");
            System.out.print("Enter your choice (1-3): ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        viewFeeStatus(studentId);
                        break;
                    case 2:
                        viewTransactionHistory(studentId);
                        break;
                    case 3:
                        System.out.println("👋 Logging out. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.err.println("Invalid choice. Please enter a number between 1 and 3.");
                }
            } else {
                System.err.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    // =========================================================
    // STUDENT UI IMPLEMENTATION METHODS
    // =========================================================

    private void viewFeeStatus(int studentId) {
        System.out.println("\n--- FEE STATUS FOR STUDENT ID " + studentId + " ---");
        try {
            // FIX: Replaced incorrect method getFeesByStudentId with correct one
            List<StudentFee> studentFees = studentFeeDAO.getStudentFeesByStudentId(studentId);

            if (studentFees.isEmpty()) {
                System.out.println("No fees have been assigned to you yet.");
                return;
            }

            // Print header
            System.out.printf("%-10s | %-12s | %-12s | %-10s | %-40s%n",
                    "ASSIGN ID", "AMOUNT", "DUE DATE", "STATUS", "DESCRIPTION");
            System.out.println("-----------------------------------------------------------------------------------");

            // Print fee details
            for (StudentFee sf : studentFees) {
                System.out.printf("%-10d | $%-11.2f | %-12s | %-10s | %-40s%n",
                        sf.getStudentFeeId(),
                        sf.getFeeAmount(),
                        sf.getDueDate().toString(),
                        sf.getStatus(),
                        sf.getDescription());
            }

        } catch (SQLException e) {
            System.err.println("Database Error: Could not retrieve fee status: " + e.getMessage());
        }
    }

    // FIX: Must iterate through all assigned fees to find all transactions
    private void viewTransactionHistory(int studentId) {
        System.out.println("\n--- TRANSACTION HISTORY FOR STUDENT ID " + studentId + " ---");
        try {
            // 1. Get all assigned fees for this student
            // FIX: Replaced incorrect method getFeesByStudentId with correct one
            List<StudentFee> assignedFees = studentFeeDAO.getStudentFeesByStudentId(studentId);

            if (assignedFees.isEmpty()) {
                System.out.println("No fees assigned, so no payment transactions expected.");
                return;
            }

            List<Transaction> transactions = new java.util.ArrayList<>();

            // 2. Iterate through each assigned fee and retrieve its transactions
            for (StudentFee sf : assignedFees) {
                // CORRECTED METHOD CALL: Uses the new DAO method
                transactions.addAll(transactionDAO.getTransactionsByStudentFeeId(sf.getStudentFeeId()));
            }

            if (transactions.isEmpty()) {
                System.out.println("No payment transactions found for your fees.");
                return;
            }

            // Print header
            System.out.printf("%-5s | %-12s | %-12s | %-25s | %-15s%n",
                    "TRX ID", "ASSIGN FEE ID", "AMOUNT", "DATE & TIME", "METHOD");
            System.out.println("----------------------------------------------------------------------");

            // Print transaction details
            for (Transaction t : transactions) {
                System.out.printf("%-5d | %-12d | $%-11.2f | %-25s | %-15s%n",
                        t.getTransactionId(),
                        t.getStudentFeeId(),
                        t.getPaymentAmount(),
                        t.getTransactionDate().toString(),
                        t.getPaymentMethod());
            }
        } catch (SQLException e) {
            System.err.println("Database Error: Could not retrieve transaction history: " + e.getMessage());
        }
    }
}