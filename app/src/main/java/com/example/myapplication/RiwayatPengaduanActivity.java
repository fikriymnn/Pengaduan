package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.RiwayatPengaduanAdapter;
import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Pengaduan;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RiwayatPengaduanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FirebaseHelper firebaseHelper;
    private List<Pengaduan> pengaduanList;
    private RiwayatPengaduanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_pengaduan);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Riwayat Pengaduan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        firebaseHelper = FirebaseHelper.getInstance();
        pengaduanList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new RiwayatPengaduanAdapter(pengaduanList, pengaduan -> {
            // Tambahkan jika ingin ke detail pengaduan
            Intent intent = new Intent(this, DetailPengaduanActivity.class);
            intent.putExtra("pengaduan_id", pengaduan.getId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUserPengaduan();
    }

    private void loadUserPengaduan() {
        String userId = firebaseHelper.getAuth().getCurrentUser().getUid();

        firebaseHelper.getUserPengaduan(userId, task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                pengaduanList.clear();

                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    Pengaduan p = doc.toObject(Pengaduan.class);
                    if (p != null) {
                        p.setId(doc.getId());
                        pengaduanList.add(p);
                    }
                }

                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(pengaduanList.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                Exception e = task.getException();
                if (e != null) e.printStackTrace();
                Toast.makeText(this, "Gagal memuat riwayat: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
