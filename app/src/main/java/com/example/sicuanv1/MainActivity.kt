package com.example.sicuanv1

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.*

// import R class dari project anda untuk mengakses res folder
import com.example.sicuanv1.R

// model & enum

enum class TransactionType { INCOME, EXPENSE }

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val nominal: Double,
    val type: TransactionType,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

// data storage (preference manager)

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sicuan_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val transactionsKey = "transaction_data"

    // menyimpan daftar transaksi ke shared preferences dalam format json
    fun saveTransactions(transactions: List<Transaction>) {
        val jsonString = gson.toJson(transactions)
        prefs.edit().putString(transactionsKey, jsonString).apply()
    }

    // mengambil data transaksi dari memori lokal dan mengubahnya kembali menjadi list
    fun getTransactions(): List<Transaction> {
        val jsonString = prefs.getString(transactionsKey, null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return try {
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // menghapus seluruh data transaksi dari memori lokal
    fun clearTransactions() {
        prefs.edit().remove(transactionsKey).apply()
    }
}

// viewmodel

class MainViewModel(private val prefManager: PreferenceManager) : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // memuat data transaksi awal dari memori saat viewmodel pertama kali dibuat
    init {
        _transactions.value = prefManager.getTransactions()
    }

    // membuat transaksi baru, menambahkannya ke daftar, dan menyimpannya ke memori
    fun addTransaction(nominal: Double, type: TransactionType, note: String) {
        val newTransaction = Transaction(nominal = nominal, type = type, note = note)
        val updatedList = _transactions.value + newTransaction
        _transactions.value = updatedList
        prefManager.saveTransactions(updatedList)
    }

    // mengosongkan state riwayat transaksi dan menghapus data di memori
    fun resetData() {
        _transactions.value = emptyList()
        prefManager.clearTransactions()
    }

    // menghitung total uang yang berstatus pemasukan
    fun getTotalIncome(): Double = _transactions.value.filter { it.type == TransactionType.INCOME }.sumOf { it.nominal }

    // menghitung total uang yang berstatus pengeluaran
    fun getTotalExpense(): Double = _transactions.value.filter { it.type == TransactionType.EXPENSE }.sumOf { it.nominal }

    // menghitung sisa saldo dengan mengurangkan pemasukan dan pengeluaran
    fun getCurrentBalance(): Double = getTotalIncome() - getTotalExpense()
}

// theme & colors

// mengatur warna dasar dan font bawaan aplikasi untuk mode gelap maupun terang
@Composable
fun SiCuanAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(primary = Color(0xFFD0BCFF), secondary = Color(0xFFCCC2DC), tertiary = Color(0xFFEFB8C8))
    } else {
        lightColorScheme(primary = Color(0xFF6650a4), secondary = Color(0xFF625b71), tertiary = Color(0xFF7D5260))
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal, fontSize = 16.sp)),
        content = content
    )
}

// main activity & navigation

class MainActivity : ComponentActivity() {
    // titik masuk utama aplikasi yang mengatur ui pertama kali
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SiCuanAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

// mengatur navigasi antar layar dan inisiasi viewmodel agar tahan rotasi layar
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val viewModel: MainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(PreferenceManager(context)) as T
            }
        }
    )

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(viewModel = viewModel, onNavigateToInput = { navController.navigate("input") })
        }
        composable("input") {
            InputScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
        }
    }
}

// screens & ui components

// menampilkan layar beranda dengan ringkasan saldo, tombol hapus, tombol tambah, dan riwayat
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToInput: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    // ambil warna dari file colors.xml
    val cuanGreen = colorResource(id = R.color.cuan_green)
    val cuanRed = colorResource(id = R.color.cuan_red)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.title_home), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.cd_reset_data), tint = cuanRed)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToInput, containerColor = cuanGreen, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.cd_add))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                BalanceCard(viewModel.getCurrentBalance(), viewModel.getTotalIncome(), viewModel.getTotalExpense())
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(id = R.string.history_title), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(id = R.string.empty_transaction), color = Color.Gray)
                    }
                }
            } else {
                items(transactions.reversed()) { TransactionItem(it) }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringResource(id = R.string.dialog_reset_title)) },
                text = { Text(stringResource(id = R.string.dialog_reset_body)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetData()
                            showResetDialog = false
                        }
                    ) { Text(stringResource(id = R.string.btn_delete_all), color = cuanRed) }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text(stringResource(id = R.string.btn_cancel)) }
                }
            )
        }
    }
}

// menampilkan kartu hijau berisi detail saldo bersih, total masuk, dan total keluar
@Composable
fun BalanceCard(balance: Double, income: Double, expense: Double) {
    val cuanGreen = colorResource(id = R.color.cuan_green)
    val cuanRedLight = colorResource(id = R.color.cuan_red_light)

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cuanGreen)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(id = R.string.total_balance), color = Color.White, fontSize = 14.sp)

            // perbaikan: tambah maxlines & ellipsis agar saldo utama tidak turun baris berantakan
            Text(
                text = formatRupiah(balance),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(20.dp))

            // perbaikan: pakai row dengan pembagian bobot (weight) yang rata 50:50
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    BalanceInfo(stringResource(id = R.string.income), income, Icons.Default.ArrowUpward, Color.White)
                }
                Box(modifier = Modifier.weight(1f)) {
                    BalanceInfo(stringResource(id = R.string.expense), expense, Icons.Default.ArrowDownward, cuanRedLight)
                }
            }
        }
    }
}

// menyusun teks nominal dan ikon di dalam kartu saldo agar rapi
@Composable
fun BalanceInfo(label: String, amount: Double, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconTint)
        Spacer(modifier = Modifier.width(4.dp))

        // perbaikan: tambah modifier weight pada column agar tidak mendesak ikon
        Column(modifier = Modifier.weight(1f)) {
            // perbaikan: batasi teks label (misal: "pengeluaran") jadi 1 baris
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // perbaikan: batasi teks angka jadi 1 baris dengan akhiran titik-titik
            Text(
                text = formatRupiah(amount),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// menampilkan satu baris riwayat yang menunjukkan keterangan dan nominal transaksi
@Composable
fun TransactionItem(transaction: Transaction) {
    val cuanGreen = colorResource(id = R.color.cuan_green)
    val cuanRed = colorResource(id = R.color.cuan_red)

    val isIncome = transaction.type == TransactionType.INCOME
    val color = if (isIncome) cuanGreen else cuanRed

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(transaction.note, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${if (isIncome) "+" else "-"} ${formatRupiah(transaction.nominal)}", color = color, fontWeight = FontWeight.Bold)
        }
    }
}

// menampilkan layar formulir untuk menginput data transaksi baru
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    var nominal by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val scrollState = rememberScrollState()
    val cuanGreen = colorResource(id = R.color.cuan_green)
    val cuanRed = colorResource(id = R.color.cuan_red)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.title_input), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.cd_back)) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TypeButton(stringResource(id = R.string.expense), selectedType == TransactionType.EXPENSE, cuanRed, { selectedType = TransactionType.EXPENSE }, Modifier.weight(1f))
                TypeButton(stringResource(id = R.string.income), selectedType == TransactionType.INCOME, cuanGreen, { selectedType = TransactionType.INCOME }, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nominal,
                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 15) nominal = it },
                label = { Text(stringResource(id = R.string.label_nominal)) },
                prefix = { Text(stringResource(id = R.string.prefix_rp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = CurrencyVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(id = R.string.label_note)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.addTransaction(nominal.toDoubleOrNull() ?: 0.0, selectedType, note)
                    onNavigateBack()
                },
                enabled = nominal.isNotBlank() && note.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cuanGreen)
            ) { Text(stringResource(id = R.string.btn_save_transaction), fontWeight = FontWeight.Bold, fontSize = 16.sp) }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// menampilkan tombol kotak yang bisa diklik untuk memilih jenis pemasukan atau pengeluaran
@Composable
fun TypeButton(text: String, isSelected: Boolean, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(if (isSelected) color else Color.LightGray.copy(alpha = 0.3f)).clickable { onClick() }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if (isSelected) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
    }
}

// utils

// mengubah angka biasa menjadi format tampilan mata uang dengan titik ribuan
fun formatRupiah(number: Double): String {
    val localeID = Locale.forLanguageTag("id-ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    numberFormat.maximumFractionDigits = 0
    return numberFormat.format(number).replace("Rp", "").trim()
}

class CurrencyVisualTransformation : VisualTransformation {
    // memformat teks angka yang sedang diketik menjadi berformat ribuan secara langsung
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formattedText = try {
            val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
            formatter.format(originalText.toLong())
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            // menentukan posisi kursor saat teks ditambahkan tanda titik
            override fun originalToTransformed(offset: Int): Int {
                var transformedOffset = 0
                var digitCount = 0
                while (digitCount < offset && transformedOffset < formattedText.length) {
                    if (formattedText[transformedOffset].isDigit()) digitCount++
                    transformedOffset++
                }
                return transformedOffset
            }

            // mengembalikan posisi kursor ke index asli jika tanda titik diabaikan
            override fun transformedToOriginal(offset: Int): Int {
                var originalOffset = 0
                for (i in 0 until offset) {
                    if (i < formattedText.length && formattedText[i].isDigit()) originalOffset++
                }
                return originalOffset.coerceAtMost(originalText.length)
            }
        }
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}