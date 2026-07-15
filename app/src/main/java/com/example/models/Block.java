package com.example.models;

/**
 * Model class for Hostel Block.
 */
public class Block {
    private int id;
    private String name;
    private String type;
    private String status;
    private String createdAt;

    public Block() {}

    public Block(int id, String name, String type, String status, String createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
