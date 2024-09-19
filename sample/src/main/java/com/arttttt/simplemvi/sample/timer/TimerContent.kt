package com.arttttt.simplemvi.sample.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arttttt.simplemvi.sample.timer.store.TimerStore
import com.arttttt.simplemvi.store.plus

@Composable
fun TimerContent() {

    val viewModel: TimerViewModel = viewModel()
    val state by viewModel.store.states.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopAppBar(
            title = {
                Text("Timer")
            },
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    enabled = !state.isTimerRunning,
                    onClick = {
                        viewModel.store + TimerStore.Intent.StartTimer
                    }
                ) {
                    Text(
                        text = if (state.value != 0 && !state.isTimerRunning) {
                            "Resume timer"
                        } else {
                            "Start timer"
                        }
                    )
                }

                Button(
                    enabled = state.isTimerRunning,
                    onClick = {
                        viewModel.store + TimerStore.Intent.StopTimer
                    }
                ) {
                    Text("Stop timer")
                }

                Button(
                    onClick = {
                        viewModel.store + TimerStore.Intent.ResetTimer
                    }
                ) {
                    Text("Reset timer")
                }
            }

            Text(
                text = "timer: ${state.value}"
            )
        }
    }
}