package com.example.models;

/**
 * Model class for Hostel Inventory and Furniture assets.
 */
public class Inventory {
    private int id;
    private String name;
    private String category; // Furniture, Beds, Chairs, Table, Fan, Light, Mattress
    private int totalStock;
    private int available;
    private int damaged;

    public Inventory() {}

    public Inventory(int id, String name, String category, int totalStock, int available, int damaged) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.totalStock = totalStock;
        this.available = available;
        this.damaged = damaged;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getTotalStock() { return totalStock; }
    public void setTotalStock(int totalStock) { this.totalStock = totalStock; }

    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }

    public int getDamaged() { return damaged; }
    public void setDamaged(int damaged) { this.damaged = damaged; }
}
