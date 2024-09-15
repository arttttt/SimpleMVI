package com.arttttt.simplemvi.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arttttt.simplemvi.sample.bottomnavigation.BottomNavigationContent
import com.arttttt.simplemvi.sample.ui.theme.SimpleMVITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SimpleMVITheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screens.BottomNavigation
                ) {
                    composable<Screens.BottomNavigation> {
                        BottomNavigationContent()
                    }
                }
            }
        }
    }
}