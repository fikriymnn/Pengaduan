package com.example.myapplication.models;

import java.util.Date;

public class Tindakan {
    private String id;
    private String pengaduanId;
    private String namaPetugas;
    private String deskripsiTindakan;
    private Date tanggalTindakan;
    private String adminId; // ID admin yang mencatat tindakan
    private Date createdAt;
    private Date updatedAt;

    public Tindakan() {
        // Default constructor required for Firestore
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Tindakan(String pengaduanId, String namaPetugas, String deskripsiTindakan, Date tanggalTindakan, String adminId) {
        this.pengaduanId = pengaduanId;
        this.namaPetugas = namaPetugas;
        this.deskripsiTindakan = deskripsiTindakan;
        this.tanggalTindakan = tanggalTindakan;
        this.adminId = adminId;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getPengaduanId() {
        return pengaduanId;
    }

    public String getNamaPetugas() {
        return namaPetugas;
    }

    public String getDeskripsiTindakan() {
        return deskripsiTindakan;
    }

    public Date getTanggalTindakan() {
        return tanggalTindakan;
    }

    public String getAdminId() {
        return adminId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setPengaduanId(String pengaduanId) {
        this.pengaduanId = pengaduanId;
    }

    public void setNamaPetugas(String namaPetugas) {
        this.namaPetugas = namaPetugas;
    }

    public void setDeskripsiTindakan(String deskripsiTindakan) {
        this.deskripsiTindakan = deskripsiTindakan;
    }

    public void setTanggalTindakan(Date tanggalTindakan) {
        this.tanggalTindakan = tanggalTindakan;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}