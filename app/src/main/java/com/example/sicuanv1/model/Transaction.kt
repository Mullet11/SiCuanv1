package com.example.sicuanv1.model

import  java.util.UUID

data class Transaction (
    // ID unik otomatis untuk data tidak tertukar pas diedit/hapus nanti
    val id: String  = UUID.randomUUID().toString(),
    //jumlah uang
    val nominal: Double,
    //tipe transaksinya
    val type: TransactionType,
    //catatan singkat
    val note: String,
    //waktu transaksi dicatat
    val timestamp: Long = System.currentTimeMillis()
)

// Enum class digunakan agar tidak salah ketik saat menentukan tipe
//disini memakai INCOME karena lebih aman daripada mengguankan String "pemasukan"
enum class TransactionType {
    INCOME,
    EXPENSE
}