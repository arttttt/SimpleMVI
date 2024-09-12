package com.arttttt.simplemvi.bottomnavigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arttttt.simplemvi.counter.CounterContent
import com.arttttt.simplemvi.notes.presentation.NotesContent
import com.arttttt.simplemvi.timer.TimerContent

@Composable
fun BottomNavigationContent() {
    var selectedTab by remember { mutableStateOf(BottomTab.COUNTER) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                BottomTab.COUNTER -> CounterContent()
                BottomTab.TIMER -> TimerContent()
                BottomTab.NOTES -> NotesContent()
            }
        }

        NavigationBar {
            BottomTab.entries.forEach { bottomTab ->
                NavigationBarItem(
                    selected = bottomTab == selectedTab,
                    onClick = {
                        selectedTab = bottomTab
                    },
                    label = {
                        Text(bottomTab.title)
                    },
                    icon = {
                        Icon(
                            imageVector = bottomTab.icon,
                            contentDescription = null,
                        )
                    }
                )
            }
        }
    }
}