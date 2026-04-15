package com.example.sicuanv1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sicuanv1.model.TransactionType
import com.example.sicuanv1.ui.theme.CuanGreen
import com.example.sicuanv1.ui.theme.CuanRed
import com.example.sicuanv1.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit // Fungsi untuk tombol kembali ke Home
) {
    // Menyimpan apa yang sedang diketik oleh pengguna
    var nominal by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) } // Default: Pengeluaran

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Catat Transaksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 1. Pilihan Tipe Transaksi (Tombol Kiri-Kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TypeButton(
                    text = "Pengeluaran",
                    isSelected = selectedType == TransactionType.EXPENSE,
                    color = CuanRed,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    modifier = Modifier.weight(1f)
                )
                TypeButton(
                    text = "Pemasukan",
                    isSelected = selectedType == TransactionType.INCOME,
                    color = CuanGreen,
                    onClick = { selectedType = TransactionType.INCOME },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Input Nominal (Uang)
            OutlinedTextField(
                value = nominal,
                onValueChange = { newValue ->
                    // Memastikan yang diketik hanya angka, membuang huruf/simbol lain
                    val digits = newValue.filter { it.isDigit() }
                    if (digits.length <= 15) { // Batas maksimal digit agar tidak error
                        nominal = digits
                    }
                },
                label = { Text("Nominal (Rp)") },
                prefix = { Text("Rp ") },
                // Keyboard khusus angka
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                // FITUR RAHASIA: Memformat angka menjadi ada titiknya (10.000)
                visualTransformation = CurrencyVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Input Keterangan
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Keterangan (Contoh: Beli Kopi)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f)) // Mendorong tombol simpan ke paling bawah layar

            // 4. Tombol Simpan
            Button(
                onClick = {
                    val nominalDouble = nominal.toDoubleOrNull() ?: 0.0
                    // Hanya simpan jika nominal lebih dari 0 dan catatan tidak kosong
                    if (nominalDouble > 0 && note.isNotBlank()) {
                        viewModel.addTransaction(nominalDouble, selectedType, note)
                        onNavigateBack() // Pindah ke Home setelah disimpan
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CuanGreen),
                // Tombol akan redup (mati) jika form belum diisi lengkap
                enabled = nominal.isNotBlank() && note.isNotBlank()
            ) {
                Text("Simpan Transaksi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// =====================================================================
// KOMPONEN PENDUKUNG
// =====================================================================

// Komponen untuk tombol Pemasukan/Pengeluaran
@Composable
fun TypeButton(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color else Color.LightGray.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.DarkGray,
            fontWeight = FontWeight.Bold
        )
    }
}

// Fungsi canggih untuk mengubah tampilan angka "10000" menjadi "10.000" secara live saat diketik
class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formattedText = try {
            val number = originalText.toLong()
            val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
            formatter.format(number)
        } catch (e: Exception) {
            originalText
        }

        // Memastikan kursor ketikan selalu berada di paling belakang
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formattedText.length
            override fun transformedToOriginal(offset: Int): Int = originalText.length
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}