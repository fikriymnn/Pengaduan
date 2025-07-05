// Final version of TindakanDialogFragment.java without redundant docRef variable
package com.example.myapplication.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;
import com.example.myapplication.helpers.FirebaseHelper;
import com.example.myapplication.models.Pengaduan;
import com.example.myapplication.models.Tindakan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TindakanDialogFragment extends DialogFragment {

    private static final String ARG_PENGADUAN = "pengaduan";

    private Pengaduan pengaduan;
    private EditText etNamaPetugas, etDeskripsiTindakan;
    private TextView tvTanggalTindakan;
    private Button btnSelectDate, btnSimpan, btnBatal;
    private FirebaseHelper firebaseHelper;
    private OnTindakanListener listener;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    public interface OnTindakanListener {
        void onTindakanCreated();
        void onError(String error);
    }

    public static TindakanDialogFragment newInstance(Pengaduan pengaduan) {
        TindakanDialogFragment fragment = new TindakanDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PENGADUAN, pengaduan);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pengaduan = (Pengaduan) getArguments().getSerializable(ARG_PENGADUAN);
        }
        firebaseHelper = FirebaseHelper.getInstance();
        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_tindakan, null);

        initViews(view);
        setupListeners();
        populateData();

        builder.setView(view);
        return builder.create();
    }

    private void initViews(View view) {
        etNamaPetugas = view.findViewById(R.id.et_nama_petugas);
        etDeskripsiTindakan = view.findViewById(R.id.et_deskripsi_tindakan);
        tvTanggalTindakan = view.findViewById(R.id.tv_tanggal_tindakan);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        btnSimpan = view.findViewById(R.id.btn_simpan);
        btnBatal = view.findViewById(R.id.btn_batal);
    }

    private void setupListeners() {
        btnSelectDate.setOnClickListener(v -> showDateTimePicker());
        btnSimpan.setOnClickListener(v -> saveTindakan());
        btnBatal.setOnClickListener(v -> dismiss());
    }

    private void populateData() {
        tvTanggalTindakan.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDate.set(Calendar.MINUTE, minute);
                    tvTanggalTindakan.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.HOUR_OF_DAY),
                selectedDate.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void saveTindakan() {
        String namaPetugas = etNamaPetugas.getText().toString().trim();
        String deskripsiTindakan = etDeskripsiTindakan.getText().toString().trim();

        if (TextUtils.isEmpty(namaPetugas)) {
            etNamaPetugas.setError("Nama petugas harus diisi");
            etNamaPetugas.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(deskripsiTindakan)) {
            etDeskripsiTindakan.setError("Deskripsi tindakan harus diisi");
            etDeskripsiTindakan.requestFocus();
            return;
        }

        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");

        String adminId = firebaseHelper.getAuth().getCurrentUser() != null ?
                firebaseHelper.getAuth().getCurrentUser().getUid() : "";

        Tindakan tindakan = new Tindakan(
                pengaduan.getId(),
                namaPetugas,
                deskripsiTindakan,
                selectedDate.getTime(),
                adminId
        );

        firebaseHelper.createTindakan(tindakan, task -> {
            if (task.isSuccessful()) {
                firebaseHelper.markPengaduanAsActioned(pengaduan.getId(), updateTask -> {
                    if (updateTask.isSuccessful()) {
                        if (listener != null) listener.onTindakanCreated();
                        dismiss();
                    } else {
                        if (listener != null)
                            listener.onError("Gagal memperbarui status pengaduan");
                        resetButton();
                    }
                });
            } else {
                if (listener != null)
                    listener.onError("Gagal menyimpan tindakan: " + task.getException().getMessage());
                resetButton();
            }
        });
    }

    private void resetButton() {
        btnSimpan.setEnabled(true);
        btnSimpan.setText("Simpan");
    }

    public void setOnTindakanListener(OnTindakanListener listener) {
        this.listener = listener;
    }
}