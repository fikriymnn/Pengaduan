package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.Kategori;
import com.example.myapplication.R;

import java.util.List;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.ViewHolder> {

    public interface KategoriActionListener {
        void onEdit(Kategori kategori);
        void onDelete(Kategori kategori);
    }

    private final List<Kategori> kategoriList;
    private final KategoriActionListener listener;

    public KategoriAdapter(List<Kategori> kategoriList, KategoriActionListener listener) {
        this.kategoriList = kategoriList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvStatus, tvDeskripsi;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tv_nama_kategori);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDeskripsi = itemView.findViewById(R.id.tv_deskripsi_kategori);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Kategori kategori, KategoriActionListener listener) {
            tvNama.setText(kategori.getName());
            tvDeskripsi.setText(kategori.getDescription());

            if (kategori.isActive()) {
                tvStatus.setText("Aktif");
                tvStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setText("Nonaktif");
                tvStatus.setTextColor(itemView.getResources().getColor(android.R.color.holo_red_dark));
            }

            btnEdit.setOnClickListener(v -> listener.onEdit(kategori));
            btnDelete.setOnClickListener(v -> listener.onDelete(kategori));
        }
    }

    @NonNull
    @Override
    public KategoriAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kategori, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull KategoriAdapter.ViewHolder holder, int position) {
        holder.bind(kategoriList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return kategoriList.size();
    }
}
