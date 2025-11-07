package ui;
import dao.TransactionDAO;
import dao.StudentFeeDAO;
import model.User;
import java.util.Scanner;
import java.sql.SQLException;
import java.sql.Timestamp;
import model.Transaction;
import model.StudentFee;
import java.util.List;
import java.math.BigDecimal;

public class AdminUI {

    private final User admin;
    private final Scanner scanner;
    private final TransactionDAO transactionDAO;
    private final StudentFeeDAO studentFeeDAO;

    public AdminUI(User admin) {
        this.admin = admin;
        this.scanner = new Scanner(System.in);
        this.transactionDAO = new TransactionDAO();
        this.studentFeeDAO = new StudentFeeDAO();
    }

    public void show() {
        System.out.println("\n--- ADMIN DASHBOARD ---");
        boolean running = true;

        while (running) {
            System.out.println("\n-----------------------------------------");
            System.out.println("  Welcome, Admin " + admin.getUsername());
            System.out.println("-----------------------------------------");
            System.out.println("1. Manage Students (Add, View, Update)");
            System.out.println("2. Manage Fees (Assign, View, Update Status)");
            System.out.println("3. View All Transactions");
            // FIX: Corrected typo from System.println to System.out.println
            System.out.println("4. Generate Reports");
            System.out.println("5. Logout");
            System.out.println("6. Record New Payment (TEST)");

            System.out.print("Enter your choice (1-6): ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {

                    case 1:
                        new StudentManagementUI(scanner).show();
                        break;
                    case 2:
                        new FeeManagementUI(scanner).show();
                        break;
                    case 3:
                        viewAllTransactions();
                        break;
                    case 4:
                        generateReports();
                        break;
                    case 5:
                        System.out.println("👋 Logging out. Goodbye!");
                        running = false;
                        break;
                    case 6:
                        recordPayment();
                        break;
                    default:
                        System.err.println("Invalid choice. Please enter a number between 1 and 6.");
                }
            } else {
                System.err.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            }
        }
    }

    // =========================================================
    // IMPLEMENTATION FOR OPTION 4: GENERATE REPORTS
    // =========================================================

    private void generateReports() {
        System.out.println("\n--- FINANCIAL REPORTS ---");

        // --- REPORT 1: Fee Status Summary ---
        System.out.println("\n[1. FEE STATUS SUMMARY]");
        try {
            // FIX: The method name was already corrected to getAllStudentFees() in a previous step
            List<StudentFee> allStudentFees = studentFeeDAO.getAllStudentFees();

            BigDecimal totalAssigned = BigDecimal.ZERO;
            BigDecimal totalPaid = BigDecimal.ZERO;
            int outstandingCount = 0;

            for (StudentFee sf : allStudentFees) {
                totalAssigned = totalAssigned.add(sf.getFeeAmount());

                if ("PAID".equalsIgnoreCase(sf.getStatus())) {
                    totalPaid = totalPaid.add(sf.getFeeAmount());
                } else if ("OVERDUE".equalsIgnoreCase(sf.getStatus()) || "OUTSTANDING".equalsIgnoreCase(sf.getStatus())) {
                    outstandingCount++;
                }
            }

            System.out.println("-------------------------------------------------------");
            System.out.printf("Total Fees Assigned: $%-10s%n", totalAssigned.toString());
            System.out.printf("Total Fees Expected Paid (PAID status): $%-10s%n", totalPaid.toString());
            System.out.printf("Total Outstanding (Assigned - Paid): $%-10s%n", totalAssigned.subtract(totalPaid).toString());
            System.out.printf("Number of Outstanding Fees: %d%n", outstandingCount);
            System.out.println("-------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database Error: Could not generate fee reports: " + e.getMessage());
        }

        // --- REPORT 2: Total Payments Recorded (from Transactions) ---
        System.out.println("\n[2. TOTAL PAYMENTS RECEIVED]");
        try {
            List<model.Transaction> allTransactions = transactionDAO.getAllTransactions();
            BigDecimal totalPaymentsReceived = BigDecimal.ZERO;

            for (model.Transaction t : allTransactions) {
                totalPaymentsReceived = totalPaymentsReceived.add(t.getPaymentAmount());
            }

            System.out.println("-------------------------------------------------------");
            System.out.printf("Total Payments Recorded: $%-10s%n", totalPaymentsReceived.toString());
            System.out.println("(Note: Sum of all transactions, regardless of fee status)");
            System.out.println("-------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database Error: Could not generate transaction reports: " + e.getMessage());
        }
    }


    // =========================================================
    // IMPLEMENTATION FOR OPTION 3 & 6
    // =========================================================

    // Implements Option 3: View All Transactions
    private void viewAllTransactions() {
        System.out.println("\n--- ALL PAYMENT TRANSACTIONS ---");
        try {
            List<Transaction> transactions = transactionDAO.getAllTransactions();

            if (transactions.isEmpty()) {
                System.out.println("No payments have been recorded yet.");
                return;
            }

            // Print header
            System.out.printf("%-5s | %-15s | %-12s | %-25s | %-15s%n",
                    "TRX ID", "STUDENT FEE ID", "AMOUNT", "DATE & TIME", "METHOD");
            System.out.println("--------------------------------------------------------------------------------");

            // Print transaction details
            for (Transaction t : transactions) {
                System.out.printf("%-5d | %-15d | $%-11.2f | %-25s | %-15s%n",
                        t.getTransactionId(),
                        t.getStudentFeeId(),
                        t.getPaymentAmount(),
                        t.getTransactionDate().toString(),
                        t.getPaymentMethod());
            }
        } catch (SQLException e) {
            System.err.println("Database Error: Could not retrieve transactions: " + e.getMessage());
        }
    }

    // Implements Option 6 (TEST): Record New Payment
    private void recordPayment() {
        System.out.println("\n--- RECORD NEW PAYMENT ---");

        System.out.print("Enter Student Fee ID (The assigned fee record ID): ");
        if (!scanner.hasNextInt()) {
            System.err.println("Invalid Student Fee ID input.");
            scanner.nextLine();
            return;
        }
        int studentFeeId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Payment Amount: ");
        BigDecimal amount;
        try {
            amount = new BigDecimal(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Invalid amount format. Payment recording cancelled.");
            return;
        }

        System.out.print("Enter Payment Method (e.g., CASH, CARD, ONLINE): ");
        String paymentMethod = scanner.nextLine().trim().toUpperCase();

        // CORRECTED: Use the constructor matching the new model
        Transaction transaction = new Transaction(
                studentFeeId,
                amount,
                Timestamp.valueOf(java.time.LocalDateTime.now()),
                paymentMethod
        );

        try {
            // CORRECTED METHOD CALL
            int newId = transactionDAO.recordPayment(transaction);
            if (newId != -1) {
                System.out.println("✅ Payment recorded successfully! Transaction ID: " + newId);
            } else {
                System.err.println("❌ Failed to record payment.");
            }
        } catch (SQLException e) {
            System.err.println("Database Error during payment recording: " + e.getMessage());
        }
    }
}