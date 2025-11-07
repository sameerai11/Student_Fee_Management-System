package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class for a single fee payment transaction.
 */
public class FeePayment {
    private int paymentId;
    private int studentFeeId; // CRITICAL: This is the ID of the assigned fee, linking back to the student.
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String receiptNumber;

    // Getters and Setters

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    // This is the correct method to link the payment back to the assignment.
    public int getStudentFeeId() { return studentFeeId; }
    public void setStudentFeeId(int studentFeeId) { this.studentFeeId = studentFeeId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }
}
