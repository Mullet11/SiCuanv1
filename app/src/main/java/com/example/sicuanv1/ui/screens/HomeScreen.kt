package com.example.sicuanv1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicuanv1.model.Transaction
import com.example.sicuanv1.model.TransactionType
import com.example.sicuanv1.ui.theme.CuanGreen
import com.example.sicuanv1.ui.theme.CuanRed
import com.example.sicuanv1.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToInput: () -> Unit // Fungsi ini akan ditarik ke Navigation nanti untuk pindah halaman
) {
    // Mengamati data dari ViewModel. Jika ada input baru, layar otomatis update!
    val transactions by viewModel.transaction.collectAsState()
    val totalBalance = viewModel.getCurrentBalance()
    val totalIncome = viewModel.getTotalIncome()
    val totalExpense = viewModel.getTotalExpense()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SiCuan", fontWeight = FontWeight.Bold) }
            )
        },
        // Ini tombol bulat (+) di pojok kanan bawah
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToInput,
                containerColor = CuanGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Memanggil Tampilan Kartu Saldo
            BalanceCard(totalBalance, totalIncome, totalExpense)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Riwayat Transaksi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 2. Menampilkan List Transaksi
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi. Yuk catat cuan pertamamu!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // .reversed() agar transaksi paling baru muncul paling atas
                    items(transactions.reversed()) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CuanGreen)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Saldo Bersih", color = Color.White, fontSize = 14.sp)
            Text(
                text = formatRupiah(balance),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bagian Pemasukan
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Pemasukan", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text(formatRupiah(income), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Bagian Pengeluaran
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFFFFCDD2))
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Pengeluaran", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text(formatRupiah(expense), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isIncome = transaction.type == TransactionType.INCOME
    val iconColor = if (isIncome) CuanGreen else CuanRed
    val icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val sign = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Kotak kecil berlatar transparan untuk ikon panah
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = transaction.note, // Menampilkan catatan (misal: "Gaji")
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }

            // Menampilkan Nominal + Warna sesuai tipe
            Text(
                text = "$sign ${formatRupiah(transaction.nominal)}",
                color = iconColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Fungsi alat bantu untuk mengubah angka 10000 menjadi Rp 10.000
fun formatRupiah(number: Double): String {
    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    numberFormat.maximumFractionDigits = 0
    return numberFormat.format(number).replace("Rp ", "Rp ")
}