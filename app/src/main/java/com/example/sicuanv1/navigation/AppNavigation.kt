package com.example.sicuanv1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sicuanv1.data.PreferenceManager
import com.example.sicuanv1.ui.screens.HomeScreen
import com.example.sicuanv1.ui.screens.InputScreen
import com.example.sicuanv1.ui.viewmodel.MainViewModel

@Composable
fun AppNavigation() {
    // navController adalah "Supir" yang akan mengantar kita pindah halaman
    val navController = rememberNavController()

    // Mempersiapkan Gudang Data dan Otak Aplikasi (ViewModel) di sini
    // agar bisa dipakai bergantian oleh HomeScreen maupun InputScreen
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val viewModel = remember { MainViewModel(prefManager) }

    // NavHost adalah "Peta"-nya. Kita tentukan halaman pertama yang dibuka adalah "home"
    NavHost(navController = navController, startDestination = "home") {

        // Rute 1: Halaman Beranda (Home)
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                // Jika tombol (+) ditekan, supir mengantar ke rute "input"
                onNavigateToInput = { navController.navigate("input") }
            )
        }

        // Rute 2: Halaman Input Transaksi
        composable("input") {
            InputScreen(
                viewModel = viewModel,
                // Jika tombol (<-) atau Simpan ditekan, supir putar balik ke halaman sebelumnya
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}