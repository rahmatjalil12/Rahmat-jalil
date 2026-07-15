package com.example.models;

/**
 * Model class for Fee structures and statuses.
 */
public class Fee {
    private int id;
    private int studentId;
    private String studentName; // joined for convenience
    private String category; // Hostel Fee, Mess Fee, Electricity, Security, Penalty
    private double amount;
    private String status; // PAID, PENDING, PARTIALLY PAID
    private String paymentDate;

    public Fee() {}

    public Fee(int id, int studentId, String studentName, String category, double amount, String status, String paymentDate) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.category = category;
        this.amount = amount;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentDate() { return paymentDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
}
