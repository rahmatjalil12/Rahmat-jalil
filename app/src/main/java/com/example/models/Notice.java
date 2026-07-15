package com.example.models;

/**
 * Model class for Announcements and Notices.
 */
public class Notice {
    private int id;
    private String title;
    private String description;
    private String priority; // HIGH, MEDIUM, LOW
    private String publishDate;
    private String expiryDate;

    public Notice() {}

    public Notice(int id, String title, String description, String priority, String publishDate, String expiryDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
