package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.PengaduanAdapter;
import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Pengaduan;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class KonfirmasiPengaduanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private List<Pengaduan> pengaduanList = new ArrayList<>();
    private PengaduanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_konfirmasi_pengaduan);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        firebaseHelper = FirebaseHelper.getInstance();

        adapter = new PengaduanAdapter(pengaduanList, pengaduan -> {
            // Intent ke halaman detail
            Intent intent = new Intent(this, DetailKonfirmasiActivity.class);
            intent.putExtra("pengaduan_id", pengaduan.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadPendingPengaduan();
    }

    private void loadPendingPengaduan() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getPendingPengaduan(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                pengaduanList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Pengaduan p = doc.toObject(Pengaduan.class);
                    p.setId(doc.getId());
                    pengaduanList.add(p);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingPengaduan(); // Refresh saat kembali
    }
}
