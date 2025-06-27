package com.example.myapplication.models;

import java.util.Date;

public class Kategori {
    private String id;
    private String name;
    private String description;
    private boolean isActive;
    private Date createdAt;

    public Kategori() {} // Required for Firestore

    public Kategori(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = true;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}