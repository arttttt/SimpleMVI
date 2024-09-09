package com.arttttt.simplemvi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arttttt.simplemvi.counter.CounterContent
import com.arttttt.simplemvi.ui.theme.SimpleMVITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SimpleMVITheme {
                CounterContent()
            }
        }
    }
}