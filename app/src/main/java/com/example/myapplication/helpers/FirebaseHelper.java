package com.example.myapplication.helpers;

import com.example.myapplication.models.Kategori;
import com.example.myapplication.models.Pengaduan;
import com.example.myapplication.models.Tindakan;
import com.example.myapplication.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.myapplication.models.Admin;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getDb() { return db; }

    // User operations
    public void createUser(User user, OnCompleteListener<Void> listener) {
        String userId = auth.getCurrentUser().getUid();
        user.setId(userId);
        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(listener);
    }

    public void getCurrentUser(OnCompleteListener<DocumentSnapshot> listener) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Pengaduan operations
    public void createPengaduan(Pengaduan pengaduan, OnCompleteListener<DocumentReference> listener) {
        db.collection("pengaduan")
                .add(pengaduan)
                .addOnCompleteListener(listener);
    }

    public void getUserPengaduan(String userId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("pengaduan")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getPendingPengaduan(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("pengaduan")
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(listener);
    }

    // Method baru untuk mendapatkan pengaduan yang sudah dikonfirmasi tapi belum ditindak
    public void getConfirmedPengaduanNotActioned(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("pengaduan")
                .whereEqualTo("status", "confirmed")
                .whereEqualTo("actionTaken", false)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getAllPengaduan(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("pengaduan")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getPengaduanByStatus(String status, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("pengaduan")
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    // Updated method to match KonfirmasiPengaduanActivity usage (without adminResponse parameter)
    public void updatePengaduanStatus(String pengaduanId, String status, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", new Date());

        // Add confirmation details if status is confirmed or rejected
        if (status.equals("confirmed") || status.equals("rejected")) {
            updates.put("confirmedAt", new Date());
            if (auth.getCurrentUser() != null) {
                updates.put("confirmedBy", auth.getCurrentUser().getUid());
            }
        }

        db.collection("pengaduan").document(pengaduanId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // Overloaded method to maintain backward compatibility
    public void updatePengaduanStatus(String pengaduanId, String status,
                                      String adminResponse, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("adminResponse", adminResponse);
        updates.put("updatedAt", new Date());

        // Add confirmation details if status is confirmed or rejected
        if (status.equals("confirmed") || status.equals("rejected")) {
            updates.put("confirmedAt", new Date());
            if (auth.getCurrentUser() != null) {
                updates.put("confirmedBy", auth.getCurrentUser().getUid());
            }
        }

        db.collection("pengaduan").document(pengaduanId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // New method to handle status update with rejection reason
    public void updatePengaduanStatusWithReason(String pengaduanId, String status,
                                                String reason, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", new Date());

        // Add rejection reason if provided
        if (reason != null && !reason.trim().isEmpty()) {
            updates.put("rejectionReason", reason.trim());
        }

        // Add confirmation details if status is confirmed or rejected
        if (status.equals("confirmed") || status.equals("rejected")) {
            updates.put("confirmedAt", new Date());
            if (auth.getCurrentUser() != null) {
                updates.put("confirmedBy", auth.getCurrentUser().getUid());
            }
        }

        db.collection("pengaduan").document(pengaduanId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // Method untuk menandai pengaduan sudah ditindak
    public void markPengaduanAsActioned(String pengaduanId, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("actionTaken", true);
        updates.put("updatedAt", new Date());

        db.collection("pengaduan").document(pengaduanId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    public void updatePengaduanId(String pengaduanId, String id, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("id", id);

        db.collection("pengaduan").document(pengaduanId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // Tindakan operations
    public void createTindakan(Tindakan tindakan, OnCompleteListener<DocumentReference> listener) {
        db.collection("tindakan")
                .add(tindakan)
                .addOnCompleteListener(listener);
    }

    public void getTindakanByPengaduanId(String pengaduanId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("tindakan")
                .whereEqualTo("pengaduanId", pengaduanId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getAllTindakan(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("tindakan")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateTindakan(String tindakanId, Tindakan tindakan, OnCompleteListener<Void> listener) {
        tindakan.setUpdatedAt(new Date());
        db.collection("tindakan").document(tindakanId)
                .set(tindakan)
                .addOnCompleteListener(listener);
    }

    public void deleteTindakan(String tindakanId, OnCompleteListener<Void> listener) {
        db.collection("tindakan").document(tindakanId)
                .delete()
                .addOnCompleteListener(listener);
    }

    // Kategori operations
    public void getKategori(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("kategori")
                .get()
                .addOnCompleteListener(listener);
    }

    public void getAllKategori(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("kategori")
                .get()
                .addOnCompleteListener(listener);
    }

    public void createKategori(Kategori kategori, OnCompleteListener<DocumentReference> listener) {
        db.collection("kategori")
                .add(kategori)
                .addOnCompleteListener(listener);
    }

    public void updateKategori(String id, Kategori kategori, OnCompleteListener<Void> listener) {
        db.collection("kategori")
                .document(id)
                .set(kategori)
                .addOnCompleteListener(listener);
    }

    public void deleteKategori(String id, OnCompleteListener<Void> listener) {
        db.collection("kategori")
                .document(id)
                .delete()
                .addOnCompleteListener(listener);
    }

    public void updateKategoriStatus(String kategoriId, boolean isActive, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", isActive);
        updates.put("updatedAt", new Date());

        db.collection("kategori").document(kategoriId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // User management operations for admin
    public void getAllUsers(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("users")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getUsersByRole(String role, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("users")
                .whereEqualTo("role", role)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateUserRole(String userId, String role, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("role", role);
        updates.put("updatedAt", new Date());

        db.collection("users").document(userId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // Statistics operations
    public void getPengaduanStatistics(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("pengaduan")
                .get()
                .addOnCompleteListener(listener);
    }

    public void getPengaduanByDateRange(Date startDate, Date endDate, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("pengaduan")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    // User profile operations
    public void updateUserProfile(Map<String, Object> updates, OnCompleteListener<Void> listener) {
        String userId = auth.getCurrentUser().getUid();
        updates.put("updatedAt", new Date());

        db.collection("users").document(userId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // Method untuk mengecek kelengkapan data user
    public void checkUserDataComplete(OnCompleteListener<DocumentSnapshot> listener) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Method untuk validasi user dapat membuat pengaduan
    public boolean isUserDataComplete(DocumentSnapshot userDocument) {
        if (!userDocument.exists()) {
            return false;
        }

        String nomorKTP = userDocument.getString("nomorKTP");
        String nomorKK = userDocument.getString("nomorKK");
        String alamat = userDocument.getString("alamat");

        return nomorKTP != null && !nomorKTP.trim().isEmpty() && nomorKTP.trim().length() == 16 &&
                nomorKK != null && !nomorKK.trim().isEmpty() && nomorKK.trim().length() == 16 &&
                alamat != null && !alamat.trim().isEmpty() && alamat.trim().length() >= 10;
    }

    // Admin operations
    public void createAdmin(Admin admin, OnCompleteListener<Void> listener) {
        String adminId = auth.getCurrentUser().getUid();
        admin.setId(adminId);
        db.collection("admins").document(adminId)
                .set(admin)
                .addOnCompleteListener(listener);
    }

    public void getCurrentAdmin(OnCompleteListener<DocumentSnapshot> listener) {
        String adminId = auth.getCurrentUser().getUid();
        db.collection("admins").document(adminId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getAllAdmins(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("admins")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getAdminsByRole(String role, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("admins")
                .whereEqualTo("role", role)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    public void updateAdminRole(String adminId, String role, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("role", role);
        updates.put("updatedAt", new Date());

        db.collection("admins").document(adminId)
                .update(updates)
                .addOnCompleteListener(listener);
    }

    // Check user type (user or admin)
    public void checkUserType(OnCompleteListener<Map<String, Object>> listener) {
        String userId = auth.getCurrentUser().getUid();

        // First check if it's a user
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && userTask.getResult().exists()) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("type", "user");
                        result.put("data", userTask.getResult().toObject(User.class));

                        // Create a successful task
                        TaskCompletionSource<Map<String, Object>> taskSource = new TaskCompletionSource<>();
                        taskSource.setResult(result);
                        listener.onComplete(taskSource.getTask());
                    } else {
                        // If not a user, check if it's an admin
                        db.collection("admins").document(userId)
                                .get()
                                .addOnCompleteListener(adminTask -> {
                                    Map<String, Object> result = new HashMap<>();
                                    if (adminTask.isSuccessful() && adminTask.getResult().exists()) {
                                        result.put("type", "admin");
                                        result.put("data", adminTask.getResult().toObject(Admin.class));
                                    } else {
                                        result.put("type", "unknown");
                                        result.put("data", null);
                                    }

                                    TaskCompletionSource<Map<String, Object>> taskSource = new TaskCompletionSource<>();
                                    taskSource.setResult(result);
                                    listener.onComplete(taskSource.getTask());
                                });
                    }
                });
    }
}