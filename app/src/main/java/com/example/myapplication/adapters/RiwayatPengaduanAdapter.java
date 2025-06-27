package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.Pengaduan;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RiwayatPengaduanAdapter extends RecyclerView.Adapter<RiwayatPengaduanAdapter.ViewHolder> {

    private List<Pengaduan> pengaduanList;
    private OnItemClickListener onItemClickListener;
    private SimpleDateFormat dateFormat;

    public interface OnItemClickListener {
        void onItemClick(Pengaduan pengaduan);
    }

    public RiwayatPengaduanAdapter(List<Pengaduan> pengaduanList, OnItemClickListener listener) {
        this.pengaduanList = pengaduanList;
        this.onItemClickListener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_riwayat_pengaduan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pengaduan pengaduan = pengaduanList.get(position);
        Context context = holder.itemView.getContext();

        // Set data
        holder.tvJudul.setText(pengaduan.getJudul());
        holder.tvKategori.setText(pengaduan.getKategoriName() != null ?
                pengaduan.getKategoriName() : pengaduan.getJenisPengaduan());
        holder.tvLokasi.setText(pengaduan.getLokasi());
        holder.tvNamaPelapor.setText("Oleh: " + pengaduan.getUserName());

        // Set tanggal
        if (pengaduan.getCreatedAt() != null) {
            holder.tvTanggal.setText(dateFormat.format(pengaduan.getCreatedAt()));
        }

        // Set status
        setupStatus(holder, pengaduan.getStatus(), context);

        // Set deskripsi (max 2 lines)
        if (pengaduan.getDeskripsi() != null && !pengaduan.getDeskripsi().isEmpty()) {
            holder.tvDeskripsi.setText(pengaduan.getDeskripsi());
            holder.tvDeskripsi.setVisibility(View.VISIBLE);
        } else {
            holder.tvDeskripsi.setVisibility(View.GONE);
        }

        // Hide priority indicator (not available in current model)
        holder.ivPriority.setVisibility(View.GONE);

        // Click listener
        holder.cardView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(pengaduan);
            }
        });
    }

    private void setupStatus(ViewHolder holder, String status, Context context) {
        if (status == null) status = "pending";

        switch (status.toLowerCase()) {
            case "pending":
                holder.tvStatus.setText("Menunggu");
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_clock);
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_pending));
                break;
            case "confirmed":
                holder.tvStatus.setText("Dikonfirmasi");
                holder.tvStatus.setBackgroundResource(R.drawable.status_confirmed_bg);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_confirmed));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_confirmed));
                break;
            case "rejected":
                holder.tvStatus.setText("Ditolak");
                holder.tvStatus.setBackgroundResource(R.drawable.status_rejected_bg);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_rejected));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_cancel);
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, R.color.status_rejected));
                break;
            default:
                holder.tvStatus.setText("Unknown");
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                holder.ivStatusIcon.setImageResource(R.drawable.ic_help);
                holder.ivStatusIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.darker_gray));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return pengaduanList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvJudul, tvKategori, tvLokasi, tvTanggal, tvStatus,
                tvDeskripsi, tvNamaPelapor;
        ImageView ivStatusIcon, ivPriority;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvJudul = itemView.findViewById(R.id.tv_judul);
            tvKategori = itemView.findViewById(R.id.tv_kategori);
            tvLokasi = itemView.findViewById(R.id.tv_lokasi);
            tvTanggal = itemView.findViewById(R.id.tv_tanggal);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDeskripsi = itemView.findViewById(R.id.tv_deskripsi);
            tvNamaPelapor = itemView.findViewById(R.id.tv_nama_pelapor);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            ivPriority = itemView.findViewById(R.id.iv_priority);
        }
    }
}