package com.arttttt.simplemvi.sample.bottomnavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(
    val title: String,
    val icon: ImageVector,
) {

    COUNTER(
        title = "Counter",
        icon = Icons.Default.Add,
    ),
    TIMER(
        title = "Timer",
        icon = Icons.Default.Refresh,
    ),
    NOTES(
        title = "Notes",
        icon = Icons.Default.Create,
    );
}