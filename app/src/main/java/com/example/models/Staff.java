package com.example.models;

/**
 * Model class for Hostel Staff.
 */
public class Staff {
    private int id;
    private String name;
    private String role; // Warden, Caretaker, Cleaner, Guard, Electrician
    private String phone;
    private String email;
    private String shift; // Morning, Evening, Night
    private String status;

    public Staff() {}

    public Staff(int id, String name, String role, String phone, String email, String shift, String status) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.email = email;
        this.shift = shift;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
