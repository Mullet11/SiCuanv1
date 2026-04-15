package com.example.sicuanv1.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sicuanv1.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Class ini fungsinya sebagai tempat penyimapanan dan mengambil data
class PreferenceManager (context: Context) {
    // variabel ini membaut atau membuka file sicuan_prefs
    private val prefs: SharedPreferences = context.getSharedPreferences("sicuan_prefs", Context.MODE_PRIVATE)
    // Inisialisasi GSON untuk mengubah Objek menjadi teks JSON dan sebaliknya
    private val gson = Gson()
    //key untuk mengenali data transaksi didalam gudang
    private val TRANSACTIONS_KEY = "transaction_data"

    // fungsi untuk menyimpan daftar transaksi disini list transaksi diubah menjadi teks JSON panjang, lalu disimpan
    fun saveTransaction (transaction: List<Transaction>) {
        val jsonString = gson.toJson(transaction)
        prefs.edit().putString(TRANSACTIONS_KEY, jsonString).apply()
    }
    // fungsi ini mengambil daftar transaksi saat aplikasi dibuka
    fun getTransactions(): List<Transaction>{
        // baris ini untuk ambil teks JSON. jika belum ada data maka akan dikembalikan null
        val jsonString = prefs.getString(TRANSACTIONS_KEY, null)
        // jika null kembalikan ke list kosong
        if (jsonString == null) {
            return emptyList()
        }
        // memberitahu GSON klo teks JSON tadi harus diubah menjadi List<Transactions>
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(jsonString, type)
    }
}
