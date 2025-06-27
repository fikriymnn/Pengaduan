package com.example.myapplication.models;

import java.io.Serializable;
import java.util.Date;

public class Pengaduan implements Serializable {
    private String id;
    private String judul;
    private String deskripsi;
    private String lokasi;
    private String jenisPengaduan; // "umum" or "kejahatan"
    private String kategoriId; // untuk jenis umum
    private String kategoriName; // nama kategori
    private String userId;
    private String userName;
    private String userPhone;
    private String userKtp;
    private String userKk;
    private String userAlamat;
    private String status; // "pending", "confirmed", "rejected"
    private String rejectReason; // alasan penolakan jika status rejected
    private boolean isActionTaken; // apakah sudah ditindak atau belum
    private Date createdAt;
    private Date updatedAt;
    private String adminId; // ID admin yang melakukan konfirmasi/penolakan
    private String imageUrl; // URL gambar jika ada

    public Pengaduan() {
        // Default constructor required for Firestore
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.status = "pending";
        this.isActionTaken = false;
    }

    public Pengaduan(String judul, String deskripsi, String lokasi, String jenisPengaduan,
                     String userId, String userName, String userPhone, String userKtp, String userKk, String userAlamat) {
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.lokasi = lokasi;
        this.jenisPengaduan = jenisPengaduan;
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.userKtp = userKtp;
        this.userKk = userKk;
        this.userAlamat = userAlamat;
        this.status = "pending";
        this.isActionTaken = false;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getJudul() {
        return judul;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getLokasi() {
        return lokasi;
    }

    public String getJenisPengaduan() {
        return jenisPengaduan;
    }

    public String getKategoriId() {
        return kategoriId;
    }

    public String getKategoriName() {
        return kategoriName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public String getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public boolean isActionTaken() {
        return isActionTaken;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public void setJenisPengaduan(String jenisPengaduan) {
        this.jenisPengaduan = jenisPengaduan;
    }

    public void setKategoriId(String kategoriId) {
        this.kategoriId = kategoriId;
    }

    public void setKategoriName(String kategoriName) {
        this.kategoriName = kategoriName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public void setActionTaken(boolean isActionTaken) {
        this.isActionTaken = isActionTaken;
        this.updatedAt = new Date();
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Helper methods
    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isConfirmed() {
        return "confirmed".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }

    public String getStatusDisplayName() {
        switch (status) {
            case "pending":
                return "Menunggu Konfirmasi";
            case "confirmed":
                return isActionTaken ? "Sudah Ditindak" : "Dikonfirmasi - Menunggu Tindakan";
            case "rejected":
                return "Ditolak";
            default:
                return "Status Tidak Diketahui";
        }
    }

    public String getJenisPengaduanDisplayName() {
        switch (jenisPengaduan) {
            case "umum":
                return "Pengaduan Umum";
            case "kejahatan":
                return "Laporan Kejahatan";
            default:
                return "Jenis Tidak Diketahui";
        }
    }

    public String getUserKtp() {
        return userKtp;
    }

    public void setUserKtp(String userKtp) {
        this.userKtp = userKtp;
    }

    public String getUserKk() {
        return userKk;
    }

    public void setUserKk(String userKk) {
        this.userKk = userKk;
    }

    public String getUserAlamat() {
        return userAlamat;
    }

    public void setUserAlamat(String userAlamat) {
        this.userAlamat = userAlamat;
    }
}