package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.myapplication.adapters.PengaduanPrintDocumentAdapter;
import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Pengaduan;
import com.example.myapplication.models.Tindakan;
import com.example.myapplication.models.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailPengaduanActivity extends AppCompatActivity {

    private static final String TAG = "DetailPengaduanActivity";

    private ImageView btnBack, btnPrint, btnShare;
    private TextView tvTitle, tvJudul, tvKategori, tvJenisPengaduan, tvLokasi,
            tvDeskripsi, tvStatus, tvNamaPelapor, tvTeleponPelapor,
            tvTanggalDibuat, tvTanggalDiupdate, tvAlasanPenolakan,
            tvTindakan, tvTanggalTindakan, tvPelaksanaTindakan, tvKK, tvKTP, tvAlamat;
    private ImageView ivFotoPengaduan, ivStatusIcon;
    private LinearLayout layoutAlasanPenolakan, layoutFotoPengaduan, layoutTindakan;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private Button btnKonfirmasi, btnTolak, btnKelolaTindakan;
    private LinearLayout layoutAdminActions;

    private FirebaseHelper firebaseHelper;
    private Pengaduan pengaduan;
    private Tindakan tindakan;
    private User user;
    private String pengaduanId;
    private boolean isFromAdmin = false;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pengaduan);

        firebaseHelper = FirebaseHelper.getInstance();
        dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));

        pengaduanId = getIntent().getStringExtra("pengaduan_id");
        isFromAdmin = getIntent().getBooleanExtra("from_admin", false);

        if (pengaduanId == null) {
            Toast.makeText(this, "ID pengaduan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadPengaduanDetail();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnPrint = findViewById(R.id.btn_print);
        btnShare = findViewById(R.id.btn_share);
        tvTitle = findViewById(R.id.tv_title);
        tvJudul = findViewById(R.id.tv_judul);
        tvKategori = findViewById(R.id.tv_kategori);
        tvJenisPengaduan = findViewById(R.id.tv_jenis_pengaduan);
        tvLokasi = findViewById(R.id.tv_lokasi);
        tvDeskripsi = findViewById(R.id.tv_deskripsi);
        tvStatus = findViewById(R.id.tv_status);
        tvNamaPelapor = findViewById(R.id.tv_nama_pelapor);
        tvTeleponPelapor = findViewById(R.id.tv_telepon_pelapor);
        tvAlamat = findViewById(R.id.tv_alamat);
        tvKK = findViewById(R.id.tv_kk);
        tvKTP = findViewById(R.id.tv_ktp);
        tvTanggalDibuat = findViewById(R.id.tv_tanggal_dibuat);
        tvTanggalDiupdate = findViewById(R.id.tv_tanggal_diupdate);
        tvAlasanPenolakan = findViewById(R.id.tv_alasan_penolakan);
        tvTindakan = findViewById(R.id.tv_tindakan);
        tvTanggalTindakan = findViewById(R.id.tv_tanggal_tindakan);
        tvPelaksanaTindakan = findViewById(R.id.tv_pelaksana_tindakan);
        ivFotoPengaduan = findViewById(R.id.iv_foto_pengaduan);
        ivStatusIcon = findViewById(R.id.iv_status_icon);
        layoutAlasanPenolakan = findViewById(R.id.layout_alasan_penolakan);
        layoutFotoPengaduan = findViewById(R.id.layout_foto_pengaduan);
        layoutTindakan = findViewById(R.id.layout_tindakan);
        progressBar = findViewById(R.id.progress_bar);
        scrollView = findViewById(R.id.scroll_view);
        btnKonfirmasi = findViewById(R.id.btn_konfirmasi);
        btnTolak = findViewById(R.id.btn_tolak);
        btnKelolaTindakan = findViewById(R.id.btn_kelola_tindakan);
        layoutAdminActions = findViewById(R.id.layout_admin_actions);

        btnKelolaTindakan.setVisibility(View.GONE);

        tvTitle.setText("Detail Pengaduan");
        btnPrint.setVisibility(isFromAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnPrint.setOnClickListener(v -> showPrintPreview());
        btnShare.setOnClickListener(v -> sharePengaduan());

        if (btnKelolaTindakan != null) {
            btnKelolaTindakan.setOnClickListener(v -> showActionDialog());
        }

        if (btnKonfirmasi != null) {
            btnKonfirmasi.setOnClickListener(v -> confirmPengaduan());
        }

        if (btnTolak != null) {
            btnTolak.setOnClickListener(v -> showRejectDialog());
        }
    }

    private void loadPengaduanDetail() {
        showLoading(true);
        Log.d(TAG, "Loading pengaduan detail for ID: " + pengaduanId);

        firebaseHelper.getDb().collection("pengaduan")
                .document(pengaduanId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            pengaduan = document.toObject(Pengaduan.class);
                            if (pengaduan != null) {
                                pengaduan.setId(document.getId());
                                Log.d(TAG, "Pengaduan loaded - Status: " + pengaduan.getStatus() + ", ActionTaken: " + pengaduan.isActionTaken());

                                // Always load tindakan data for confirmed pengaduan, regardless of actionTaken status
                                if (pengaduan.isConfirmed()) {
                                    loadTindakanData();
                                } else {
                                    showLoading(false);
                                    displayPengaduanDetail();
                                }
                            }
                        } else {
                            showLoading(false);
                            Toast.makeText(this, "Pengaduan tidak ditemukan", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        showLoading(false);
                        Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading pengaduan", task.getException());
                        finish();
                    }
                });
    }

    private void loadTindakanData() {
        Log.d(TAG, "Loading tindakan data for pengaduan ID: " + pengaduanId);

        firebaseHelper.getDb().collection("tindakan")
                .whereEqualTo("pengaduanId", pengaduanId)
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        tindakan = document.toObject(Tindakan.class);
                        if (tindakan != null) {
                            tindakan.setId(document.getId());
                            Log.d(TAG, "Tindakan data loaded: " + tindakan.getDeskripsiTindakan());

                            // Update pengaduan actionTaken status if tindakan exists but pengaduan shows actionTaken=false
                            if (!pengaduan.isActionTaken()) {
                                Log.d(TAG, "Updating pengaduan actionTaken status to true");
                                updatePengaduanActionStatus(true);
                            }
                        }
                    } else {
                        Log.d(TAG, "No tindakan data found for pengaduan ID: " + pengaduanId);
                    }
                    displayPengaduanDetail();
                });
    }

    private void updatePengaduanActionStatus(boolean actionTaken) {
        firebaseHelper.getDb().collection("pengaduan")
                .document(pengaduanId)
                .update("actionTaken", actionTaken)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Pengaduan actionTaken status updated to: " + actionTaken);
                    pengaduan.setActionTaken(actionTaken);
                    displayPengaduanDetail(); // Refresh display
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating pengaduan actionTaken status", e);
                });
    }

    private void displayPengaduanDetail() {
        if (pengaduan == null) return;

        Log.d(TAG, "Displaying pengaduan detail - Status: " + pengaduan.getStatus() + ", ActionTaken: " + pengaduan.isActionTaken());

        tvJudul.setText(pengaduan.getJudul());
        tvKategori.setText(pengaduan.getKategoriName() != null ? pengaduan.getKategoriName() : pengaduan.getJenisPengaduan());
        tvJenisPengaduan.setText(pengaduan.getJenisPengaduanDisplayName());
        tvLokasi.setText(pengaduan.getLokasi());
        tvDeskripsi.setText(pengaduan.getDeskripsi());
        tvNamaPelapor.setText(pengaduan.getUserName());
        tvTeleponPelapor.setText(pengaduan.getUserPhone());
        tvKTP.setText(pengaduan.getUserKtp());
        tvKK.setText(pengaduan.getUserKk());
        tvAlamat.setText(pengaduan.getUserAlamat());

        // Update status display with proper logic
        updateStatusDisplay();

        // Set dates
        if (pengaduan.getCreatedAt() != null) {
            tvTanggalDibuat.setText(dateFormat.format(pengaduan.getCreatedAt()));
        }
        if (pengaduan.getUpdatedAt() != null) {
            tvTanggalDiupdate.setText(dateFormat.format(pengaduan.getUpdatedAt()));
        }

        // Display rejection reason if rejected
        if (pengaduan.isRejected() && pengaduan.getRejectReason() != null) {
            layoutAlasanPenolakan.setVisibility(View.VISIBLE);
            tvAlasanPenolakan.setText(pengaduan.getRejectReason());
        } else {
            layoutAlasanPenolakan.setVisibility(View.GONE);
        }

        // Display action data
        displayActionData();

        // Display image if available
        if (pengaduan.getImageUrl() != null && !pengaduan.getImageUrl().isEmpty()) {
            layoutFotoPengaduan.setVisibility(View.VISIBLE);
            Glide.with(this).load(pengaduan.getImageUrl()).into(ivFotoPengaduan);
        } else {
            layoutFotoPengaduan.setVisibility(View.GONE);
        }

        // Show admin actions if needed
        showAdminActions();
    }

    private void updateStatusDisplay() {
        String statusText;
        int statusBg;
        int statusColor;

        if (pengaduan.isRejected()) {
            statusText = "Ditolak";
            statusBg = R.drawable.status_rejected_bg;
            statusColor = R.color.status_rejected;
        } else if (pengaduan.isConfirmed()) {
            if (tindakan != null || pengaduan.isActionTaken()) {
                statusText = "Dikonfirmasi - Sudah Ditindak";
                statusBg = R.drawable.status_confirmed_bg;
                statusColor = R.color.status_confirmed;
            } else {
                statusText = "Dikonfirmasi - Menunggu Tindakan";
                statusBg = R.drawable.status_confirmed_bg;
                statusColor = R.color.status_confirmed;
            }
        } else {
            statusText = "Menunggu Konfirmasi";
            statusBg = R.drawable.status_pending_bg;
            statusColor = R.color.status_pending;
        }

        tvStatus.setText(statusText);
        tvStatus.setBackground(ContextCompat.getDrawable(this, statusBg));
        tvStatus.setTextColor(ContextCompat.getColor(this, statusColor));
    }

    private void displayActionData() {
        Log.d(TAG, "displayActionData - tindakan: " + (tindakan != null ? "exists" : "null") +
                ", pengaduan.isActionTaken(): " + pengaduan.isActionTaken());

        if (pengaduan.isConfirmed() && tindakan != null) {
            layoutTindakan.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing tindakan layout");

            // Display action description
            tvTindakan.setText(tindakan.getDeskripsiTindakan() != null ?
                    tindakan.getDeskripsiTindakan() : "-");

            // Display action date
            if (tindakan.getTanggalTindakan() != null) {
                tvTanggalTindakan.setText(dateFormat.format(tindakan.getTanggalTindakan()));
            } else if (tindakan.getCreatedAt() != null) {
                tvTanggalTindakan.setText(dateFormat.format(tindakan.getCreatedAt()));
            } else {
                tvTanggalTindakan.setText("-");
            }

            // Display action performer
            tvPelaksanaTindakan.setText(tindakan.getNamaPetugas() != null ?
                    tindakan.getNamaPetugas() : "-");
        } else {
            layoutTindakan.setVisibility(View.GONE);
            Log.d(TAG, "Hiding tindakan layout");
        }
    }

    private void showAdminActions() {
        if (!isFromAdmin) {
            layoutAdminActions.setVisibility(View.GONE);
            if (btnKelolaTindakan != null) btnKelolaTindakan.setVisibility(View.GONE);
            return;
        }

        if (pengaduan.isPending()) {
            // Show confirm/reject buttons for pending reports
            layoutAdminActions.setVisibility(View.VISIBLE);
            if (btnKelolaTindakan != null) btnKelolaTindakan.setVisibility(View.GONE);
        } else if (pengaduan.isConfirmed() && tindakan == null) {
            // Show action button for confirmed reports without action
            layoutAdminActions.setVisibility(View.GONE);
            if (btnKelolaTindakan != null) btnKelolaTindakan.setVisibility(View.GONE);
        } else {
            // Hide all action buttons for rejected or already acted upon reports
            layoutAdminActions.setVisibility(View.GONE);
            if (btnKelolaTindakan != null) btnKelolaTindakan.setVisibility(View.GONE);
        }
    }

    private void confirmPengaduan() {
        // Implementation for confirming pengaduan
        showLoading(true);
        firebaseHelper.getDb().collection("pengaduan")
                .document(pengaduanId)
                .update("status", "confirmed")
                .addOnSuccessListener(aVoid -> {
                    pengaduan.setStatus("confirmed");
                    showLoading(false);
                    displayPengaduanDetail();
                    Toast.makeText(this, "Pengaduan berhasil dikonfirmasi", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Gagal konfirmasi pengaduan", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error confirming pengaduan", e);
                });
    }

    private void showRejectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditText input = new EditText(this);
        input.setHint("Masukkan alasan penolakan");

        builder.setTitle("Tolak Pengaduan")
                .setMessage("Berikan alasan penolakan:")
                .setView(input)
                .setPositiveButton("Tolak", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (!reason.isEmpty()) {
                        rejectPengaduan(reason);
                    } else {
                        Toast.makeText(this, "Alasan penolakan harus diisi", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void rejectPengaduan(String reason) {
        showLoading(true);
        firebaseHelper.getDb().collection("pengaduan")
                .document(pengaduanId)
                .update("status", "rejected", "rejectReason", reason)
                .addOnSuccessListener(aVoid -> {
                    pengaduan.setStatus("rejected");
                    pengaduan.setRejectReason(reason);
                    showLoading(false);
                    displayPengaduanDetail();
                    Toast.makeText(this, "Pengaduan berhasil ditolak", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Gagal menolak pengaduan", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error rejecting pengaduan", e);
                });
    }

    private void showActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tindakan, null);

        EditText etNamaPetugas = dialogView.findViewById(R.id.et_nama_petugas);
        EditText etDeskripsiTindakan = dialogView.findViewById(R.id.et_deskripsi_tindakan);
        DatePicker datePicker = dialogView.findViewById(R.id.tv_tanggal_tindakan);

        builder.setTitle("Catat Tindakan")
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialog, which) -> {
                    String namaPetugas = etNamaPetugas.getText().toString().trim();
                    String deskripsi = etDeskripsiTindakan.getText().toString().trim();

                    if (namaPetugas.isEmpty() || deskripsi.isEmpty()) {
                        Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Date tanggalTindakan = new Date(datePicker.getYear() - 1900,
                            datePicker.getMonth(), datePicker.getDayOfMonth());

                    saveTindakan(namaPetugas, deskripsi, tanggalTindakan);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void saveTindakan(String namaPetugas, String deskripsi, Date tanggalTindakan) {
        showLoading(true);

        Tindakan newTindakan = new Tindakan(pengaduanId, namaPetugas, deskripsi, tanggalTindakan, "admin_id");

        firebaseHelper.getDb().collection("tindakan")
                .add(newTindakan)
                .addOnSuccessListener(documentReference -> {
                    // Update pengaduan actionTaken status
                    firebaseHelper.getDb().collection("pengaduan")
                            .document(pengaduanId)
                            .update("actionTaken", true)
                            .addOnSuccessListener(aVoid -> {
                                tindakan = newTindakan;
                                tindakan.setId(documentReference.getId());
                                pengaduan.setActionTaken(true);
                                showLoading(false);
                                displayPengaduanDetail();
                                Toast.makeText(this, "Tindakan berhasil dicatat", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                showLoading(false);
                                Toast.makeText(this, "Gagal update status pengaduan", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error updating pengaduan actionTaken", e);
                            });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Gagal menyimpan tindakan", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving tindakan", e);
                });
    }

    private void showPrintPreview() {
        if (pengaduan == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View previewView = createPrintPreviewView();

        builder.setTitle("Preview Dokumen")
                .setView(previewView)
                .setPositiveButton("Print", (dialog, which) -> printDocument())
                .setNeutralButton("Simpan PDF", (dialog, which) -> saveToPDF())
                .setNegativeButton("Batal", null)
                .show();
    }

    private View createPrintPreviewView() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("DETAIL PENGADUAN");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        addPrintRow(layout, "Judul", pengaduan.getJudul());
        addPrintRow(layout, "Kategori", pengaduan.getKategoriName() != null ? pengaduan.getKategoriName() : pengaduan.getJenisPengaduan());
        addPrintRow(layout, "Jenis", pengaduan.getJenisPengaduanDisplayName());
        addPrintRow(layout, "Lokasi", pengaduan.getLokasi());
        addPrintRow(layout, "Deskripsi", pengaduan.getDeskripsi());
        addPrintRow(layout, "Status", pengaduan.getStatusDisplayName());
        addPrintRow(layout, "Nama Pelapor", pengaduan.getUserName());
        addPrintRow(layout, "Telepon", pengaduan.getUserPhone());
        if (pengaduan.getCreatedAt() != null)
            addPrintRow(layout, "Tanggal Dibuat", dateFormat.format(pengaduan.getCreatedAt()));

        // Add action data to print preview
        if (pengaduan.isConfirmed() && tindakan != null) {
            addPrintRow(layout, "Tindakan", tindakan.getDeskripsiTindakan() != null ? tindakan.getDeskripsiTindakan() : "-");
            if (tindakan.getTanggalTindakan() != null) {
                addPrintRow(layout, "Tanggal Tindakan", dateFormat.format(tindakan.getTanggalTindakan()));
            } else if (tindakan.getCreatedAt() != null) {
                addPrintRow(layout, "Tanggal Tindakan", dateFormat.format(tindakan.getCreatedAt()));
            }
            addPrintRow(layout, "Pelaksana", tindakan.getNamaPetugas() != null ? tindakan.getNamaPetugas() : "-");
        }

        scrollView.addView(layout);
        return scrollView;
    }

    private void addPrintRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView labelView = new TextView(this);
        labelView.setText(label + ":");
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView valueView = new TextView(this);
        valueView.setText(value != null ? value : "-");
        valueView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f));

        row.addView(labelView);
        row.addView(valueView);
        parent.addView(row);
    }

    private void printDocument() {
        if (pengaduan == null) return;
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = "Pengaduan_" + pengaduan.getId();
        PrintDocumentAdapter adapter = new PengaduanPrintDocumentAdapter(this, pengaduan);
        printManager.print(jobName, adapter, new PrintAttributes.Builder().build());
    }

    private void saveToPDF() {
        if (pengaduan == null) return;
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(12);

            int y = 50;
            int lineHeight = 20;
            paint.setTextSize(18);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            canvas.drawText("DETAIL PENGADUAN", 50, y, paint);
            y += lineHeight * 2;

            paint.setTextSize(12);
            paint.setTypeface(Typeface.DEFAULT);

            canvas.drawText("Judul: " + pengaduan.getJudul(), 50, y, paint); y += lineHeight;
            canvas.drawText("Kategori: " + (pengaduan.getKategoriName() != null ? pengaduan.getKategoriName() : pengaduan.getJenisPengaduan()), 50, y, paint); y += lineHeight;
            canvas.drawText("Jenis: " + pengaduan.getJenisPengaduanDisplayName(), 50, y, paint); y += lineHeight;
            canvas.drawText("Lokasi: " + pengaduan.getLokasi(), 50, y, paint); y += lineHeight;
            canvas.drawText("Status: " + pengaduan.getStatusDisplayName(), 50, y, paint); y += lineHeight;
            canvas.drawText("Nama Pelapor: " + pengaduan.getUserName(), 50, y, paint); y += lineHeight;
            canvas.drawText("Telepon: " + pengaduan.getUserPhone(), 50, y, paint); y += lineHeight;
            if (pengaduan.getCreatedAt() != null) {
                canvas.drawText("Tanggal: " + dateFormat.format(pengaduan.getCreatedAt()), 50, y, paint); y += lineHeight;
            }

            // Add action data to PDF
            if (pengaduan.isConfirmed() && tindakan != null) {
                canvas.drawText("Tindakan: " + (tindakan.getDeskripsiTindakan() != null ? tindakan.getDeskripsiTindakan() : "-"), 50, y, paint); y += lineHeight;
                if (tindakan.getTanggalTindakan() != null) {
                    canvas.drawText("Tanggal Tindakan: " + dateFormat.format(tindakan.getTanggalTindakan()), 50, y, paint); y += lineHeight;
                } else if (tindakan.getCreatedAt() != null) {
                    canvas.drawText("Tanggal Tindakan: " + dateFormat.format(tindakan.getCreatedAt()), 50, y, paint); y += lineHeight;
                }
                canvas.drawText("Pelaksana: " + (tindakan.getNamaPetugas() != null ? tindakan.getNamaPetugas() : "-"), 50, y, paint); y += lineHeight;
            }

            canvas.drawText("Deskripsi:", 50, y, paint); y += lineHeight;
            String desc = pengaduan.getDeskripsi() != null ? pengaduan.getDeskripsi() : "-";
            for (String line : desc.split("\\n")) {
                canvas.drawText(line, 70, y, paint);
                y += lineHeight;
            }

            pdfDocument.finishPage(page);
            String fileName = "Pengaduan_" + pengaduan.getId() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

            Toast.makeText(this, "PDF berhasil disimpan", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error membuat PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void sharePengaduan() {
        if (pengaduan == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("DETAIL PENGADUAN\n\n")
                .append("Judul: ").append(pengaduan.getJudul()).append("\n")
                .append("Kategori: ").append(pengaduan.getKategoriName() != null ? pengaduan.getKategoriName() : pengaduan.getJenisPengaduan()).append("\n")
                .append("Jenis: ").append(pengaduan.getJenisPengaduanDisplayName()).append("\n")
                .append("Lokasi: ").append(pengaduan.getLokasi()).append("\n")
                .append("Status: ").append(pengaduan.getStatusDisplayName()).append("\n")
                .append("Nama Pelapor: ").append(pengaduan.getUserName()).append("\n")
                .append("Telepon: ").append(pengaduan.getUserPhone()).append("\n");

        // Add action data to share content
        if (pengaduan.isActionTaken() && tindakan != null) {
            sb.append("Tindakan: ").append(tindakan.getDeskripsiTindakan() != null ? tindakan.getDeskripsiTindakan() : "-").append("\n");
            if (tindakan.getTanggalTindakan() != null) {
                sb.append("Tanggal Tindakan: ").append(dateFormat.format(tindakan.getTanggalTindakan())).append("\n");
            } else if (tindakan.getCreatedAt() != null) {
                sb.append("Tanggal Tindakan: ").append(dateFormat.format(tindakan.getCreatedAt())).append("\n");
            }
            sb.append("Pelaksana: ").append(tindakan.getNamaPetugas() != null ? tindakan.getNamaPetugas() : "-").append("\n");
        }

        sb.append("Deskripsi: ").append(pengaduan.getDeskripsi()).append("\n");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Detail Pengaduan");
        startActivity(Intent.createChooser(shareIntent, "Bagikan Pengaduan"));
    }
}