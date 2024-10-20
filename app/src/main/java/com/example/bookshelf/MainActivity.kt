package com.example.bookshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.bookshelf.ui.BookShelfApp
import com.example.bookshelf.ui.theme.BookShelfTheme

//TODO 全体を軽くする

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BookShelfTheme {
                Surface (modifier = Modifier.fillMaxSize()) {
                    BookShelfApp()
                }
            }
        }
    }
}

