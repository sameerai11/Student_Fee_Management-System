package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {

    // Fields corresponding to the Transactions table
    private int transactionId;
    private int studentFeeId;       // CORRECTED: Link to the StudentFees table
    private BigDecimal paymentAmount;
    private Timestamp transactionDate;
    private String paymentMethod;

    // -------------------------------------------------------------------
    // 1. Constructors
    // -------------------------------------------------------------------

    // Default Constructor
    public Transaction() {
    }

    /**
     * Constructor for DAO Retrieval (includes auto-generated ID).
     */
    public Transaction(int transactionId, int studentFeeId, BigDecimal paymentAmount, Timestamp transactionDate, String paymentMethod) {
        this.transactionId = transactionId;
        this.studentFeeId = studentFeeId;
        this.paymentAmount = paymentAmount;
        this.transactionDate = transactionDate;
        this.paymentMethod = paymentMethod;
    }

    /**
     * Constructor for RECORDING a NEW transaction (used by Controller).
     * Arguments: (studentFeeId, paymentAmount, transactionDate, paymentMethod)
     */
    public Transaction(int studentFeeId, BigDecimal paymentAmount, Timestamp transactionDate, String paymentMethod) {
        // transactionId is handled by the DAO
        this.studentFeeId = studentFeeId;
        this.paymentAmount = paymentAmount;
        this.transactionDate = transactionDate;
        this.paymentMethod = paymentMethod;
    }

    // -------------------------------------------------------------------
    // 2. Getters and Setters
    // -------------------------------------------------------------------

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getStudentFeeId() {
        return studentFeeId;
    }

    public void setStudentFeeId(int studentFeeId) {
        this.studentFeeId = studentFeeId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}