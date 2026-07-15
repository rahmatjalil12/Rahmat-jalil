package com.example.models;

/**
 * Model class for Hostel Room.
 */
public class Room {
    private int id;
    private int blockId;
    private int floorId;
    private String roomNumber;
    private int capacity;
    private int occupiedSeats;
    private int availableSeats;
    private String roomType;
    private String status;
    private String notes;

    public Room() {}

    public Room(int id, int blockId, int floorId, String roomNumber, int capacity, int occupiedSeats, int availableSeats, String roomType, String status, String notes) {
        this.id = id;
        this.blockId = blockId;
        this.floorId = floorId;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.occupiedSeats = occupiedSeats;
        this.availableSeats = availableSeats;
        this.roomType = roomType;
        this.status = status;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBlockId() { return blockId; }
    public void setBlockId(int blockId) { this.blockId = blockId; }

    public int getFloorId() { return floorId; }
    public void setFloorId(int floorId) { this.floorId = floorId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getOccupiedSeats() { return occupiedSeats; }
    public void setOccupiedSeats(int occupiedSeats) { this.occupiedSeats = occupiedSeats; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
