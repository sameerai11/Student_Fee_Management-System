package model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.sql.Date;

public class StudentFee {

    // --- 1. JavaFX Properties for TableView binding (MATCHING DB SCHEMA) ---
    private final IntegerProperty studentFeeId;
    private final IntegerProperty feeId;
    private final IntegerProperty studentId;
    private final ObjectProperty<BigDecimal> feeAmount;
    private final ObjectProperty<Date> dueDate;
    private final StringProperty status;
    private final StringProperty description;

    // NOTE: amountPaid and balanceDue properties are REMOVED as they do not exist
    // in the 'studentfees' database table, preventing DAO errors.

    // ----------------------------------------------------
    // 2. Constructors (Simplified to match 6 database fields)
    // ----------------------------------------------------

    /** Default constructor for JavaFX and new assignments. */
    public StudentFee() {
        this.studentFeeId = new SimpleIntegerProperty();
        this.feeId = new SimpleIntegerProperty();
        this.studentId = new SimpleIntegerProperty();
        this.feeAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
        this.dueDate = new SimpleObjectProperty<>();
        this.status = new SimpleStringProperty();
        this.description = new SimpleStringProperty();
    }

    /** Constructor used for RETRIEVING a fee from the database (6 fields). */
    public StudentFee(int studentFeeId, int feeId, int studentId, BigDecimal feeAmount, Date dueDate, String status, String description) {
        this.studentFeeId = new SimpleIntegerProperty(studentFeeId);
        this.feeId = new SimpleIntegerProperty(feeId);
        this.studentId = new SimpleIntegerProperty(studentId);
        this.feeAmount = new SimpleObjectProperty<>(feeAmount);
        this.dueDate = new SimpleObjectProperty<>(dueDate);
        this.status = new SimpleStringProperty(status);
        this.description = new SimpleStringProperty(description);
    }


    // ----------------------------------------------------
    // 3. Property Getters (CRITICAL for TableView)
    // ----------------------------------------------------

    public IntegerProperty studentFeeIdProperty() { return studentFeeId; }
    public IntegerProperty feeIdProperty() { return feeId; }
    public IntegerProperty studentIdProperty() { return studentId; }
    public ObjectProperty<BigDecimal> feeAmountProperty() { return feeAmount; }
    public ObjectProperty<Date> dueDateProperty() { return dueDate; }
    public StringProperty statusProperty() { return status; }
    public StringProperty descriptionProperty() { return description; }

    // NOTE: amountPaidProperty and balanceDueProperty are REMOVED.

    // ----------------------------------------------------
    // 4. Standard Getters and Setters
    // ----------------------------------------------------

    public int getStudentFeeId() { return studentFeeId.get(); }
    public void setStudentFeeId(int studentFeeId) { this.studentFeeId.set(studentFeeId); }

    public int getFeeId() { return feeId.get(); }
    public void setFeeId(int feeId) { this.feeId.set(feeId); }

    public int getStudentId() { return studentId.get(); }
    public void setStudentId(int studentId) { this.studentId.set(studentId); }

    public BigDecimal getFeeAmount() { return feeAmount.get(); }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount.set(feeAmount); }

    // Removed get/setAmountPaid and getBalanceDue

    public Date getDueDate() { return dueDate.get(); }
    public void setDueDate(Date dueDate) { this.dueDate.set(dueDate); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
}