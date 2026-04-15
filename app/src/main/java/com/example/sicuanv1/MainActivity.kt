package com.example.sicuanv1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sicuanv1.navigation.AppNavigation
import com.example.sicuanv1.ui.theme.SiCuanv1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Menggunakan tema bawaan aplikasi
            SiCuanv1Theme {
                // Surface adalah dasar dari layar aplikasi
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // MEMANGGIL JEMBATAN NAVIGASI KITA
                    AppNavigation()
                }
            }
        }
    }
}