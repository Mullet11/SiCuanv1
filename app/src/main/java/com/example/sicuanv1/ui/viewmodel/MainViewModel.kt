package com.example.sicuanv1.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.sicuanv1.data.PreferenceManager
import com.example.sicuanv1.model.Transaction
import com.example.sicuanv1.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(private val prefManager: PreferenceManager) : ViewModel() {

    // _transactions adalah data asli yang bisa diubah-ubah dari dalam viewmodel
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    // transaction (tanpa garis bawah ini) adalah data yg hanya bisa dibaca oleh UI
    val transaction: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        //saat aplikasi pertama kali dibuka, lngsng muat data dari SharedPreferences
        _transactions.value = prefManager.getTransactions()
    }
    //fungsi ini untuk menambahkan transaksi baru dari form input
    fun addTranscation(nominal: Double, type: TransactionType, note: String) {
        //ini untuk buat objek transaksi baru
        val newTransaction = Transaction(
            nominal = nominal,
            type = type,
            note = note
        )
        //ini untuk gabungkan data lama dengan data baru
        val updatedList = _transactions.value + newTransaction
        // update tampilan UI secara real-time
        _transactions.value = updatedList
        // ini untuk menyimpan ke HP agar tidak hilang saat ditutup
        prefManager.saveTransaction(updatedList)
    }
    //Fungsi untuk menghitung total pemasukan
    fun getTotalIncome(): Double {
        return _transactions.value
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.nominal }
    }

    fun getTotalExpense(): Double {
        return _transactions.value
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.nominal }
    }
    //fungsi untuk menghitung total pengeluaran
    fun getCurrentBalance(): Double {
        return getTotalIncome() - getTotalExpense()
    }

    //fungsi untuk menambahkan transaksi
    fun addTransaction(nominal: Double, type: TransactionType, note: String){
        val newTransaction = Transaction(
            nominal = nominal,
            type = type,
            note = note
        )
        val updatedList = _transactions.value + newTransaction
        _transactions.value = updatedList
        prefManager.saveTransaction(updatedList)
    }
}
