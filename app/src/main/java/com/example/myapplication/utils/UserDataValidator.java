package com.example.myapplication.utils;

import com.google.firebase.firestore.DocumentSnapshot;

public class UserDataValidator {

    /**
     * Validates if KTP number is valid
     *
     * @param nomorKTP KTP number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidKTP(String nomorKTP) {
        if (nomorKTP == null || nomorKTP.trim().isEmpty()) {
            return false;
        }

        String ktpTrimmed = nomorKTP.trim();

        // Must be exactly 16 digits
        if (ktpTrimmed.length() != 16) {
            return false;
        }

        // Must contain only numbers
        return ktpTrimmed.matches("\\d+");
    }

    /**
     * Validates if KK number is valid
     *
     * @param nomorKK KK number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidKK(String nomorKK) {
        if (nomorKK == null || nomorKK.trim().isEmpty()) {
            return false;
        }

        String kkTrimmed = nomorKK.trim();

        // Must be exactly 16 digits
        if (kkTrimmed.length() != 16) {
            return false;
        }

        // Must contain only numbers
        return kkTrimmed.matches("\\d+");
    }

    /**
     * Validates if address is valid
     *
     * @param alamat Address to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidAlamat(String alamat) {
        if (alamat == null || alamat.trim().isEmpty()) {
            return false;
        }

        String alamatTrimmed = alamat.trim();

        // Must be at least 10 characters
        return alamatTrimmed.length() >= 10;
    }

    /**
     * Checks if user has complete data required for creating pengaduan
     *
     * @param userDocument Firebase document snapshot of user
     * @return true if user data is complete, false otherwise
     */
    public static boolean isUserDataComplete(DocumentSnapshot userDocument) {
        if (!userDocument.exists()) {
            return false;
        }

        String nomorKTP = userDocument.getString("nomorKTP");
        String nomorKK = userDocument.getString("nomorKK");
        String alamat = userDocument.getString("alamat");

        return isValidKTP(nomorKTP) && isValidKK(nomorKK) && isValidAlamat(alamat);
    }

    /**
     * Gets validation error message for KTP
     *
     * @param nomorKTP KTP number to validate
     * @return error message or null if valid
     */
    public static String getKTPErrorMessage(String nomorKTP) {
        if (nomorKTP == null || nomorKTP.trim().isEmpty()) {
            return "Nomor KTP tidak boleh kosong";
        }

        String ktpTrimmed = nomorKTP.trim();

        if (ktpTrimmed.length() != 16) {
            return "Nomor KTP harus 16 digit";
        }

        if (!ktpTrimmed.matches("\\d+")) {
            return "Nomor KTP hanya boleh berisi angka";
        }

        return null; // Valid
    }

    /**
     * Gets validation error message for KK
     *
     * @param nomorKK KK number to validate
     * @return error message or null if valid
     */
    public static String getKKErrorMessage(String nomorKK) {
        if (nomorKK == null || nomorKK.trim().isEmpty()) {
            return "Nomor KK tidak boleh kosong";
        }

        String kkTrimmed = nomorKK.trim();

        if (kkTrimmed.length() != 16) {
            return "Nomor KK harus 16 digit";
        }

        if (!kkTrimmed.matches("\\d+")) {
            return "Nomor KK hanya boleh berisi angka";
        }

        return null; // Valid
    }

    /**
     * Gets validation error message for address
     *
     * @param alamat Address to validate
     * @return error message or null if valid
     */
    public static String getAlamatErrorMessage(String alamat) {
        if (alamat == null || alamat.trim().isEmpty()) {
            return "Alamat tidak boleh kosong";
        }

        String alamatTrimmed = alamat.trim();

        if (alamatTrimmed.length() < 10) {
            return "Alamat minimal 10 karakter";
        }

        return null; // Valid
    }
}