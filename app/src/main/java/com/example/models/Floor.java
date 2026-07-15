package com.example.models;

/**
 * Model class for Hostel Floor.
 */
public class Floor {
    private int id;
    private int blockId;
    private int number;
    private String status;

    public Floor() {}

    public Floor(int id, int blockId, int number, String status) {
        this.id = id;
        this.blockId = blockId;
        this.number = number;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
