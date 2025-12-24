package com.example.layout_optimization.models;

public class Item {
    private String title;
    private String description;
    private int iconRes;
    private String timestamp;
    
    public Item(String title, String description, int iconRes, String timestamp) {
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public String getTimestamp() { return timestamp; }
}
