package com.example.myapplication.models;

import java.util.Date;

public class User {
    private String id;
    private String name;
    private String email;
    private String role;
    private String phoneNumber;
    private String nomorKTP;
    private String nomorKK;
    private String alamat;
    private Date createdAt;
    private Date updatedAt;

    public User() {} // Required for Firestore

    public User(String name, String email, String role, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getNomorKTP() { return nomorKTP; }
    public void setNomorKTP(String nomorKTP) { this.nomorKTP = nomorKTP; }

    public String getNomorKK() { return nomorKK; }
    public void setNomorKK(String nomorKK) { this.nomorKK = nomorKK; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}