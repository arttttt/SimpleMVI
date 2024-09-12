package com.arttttt.simplemvi.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arttttt.simplemvi.plus

@Composable
fun TimerContent() {

    val viewModel: TimerViewModel = viewModel()
    val state by viewModel.store.states.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.store + TimerStore.Intent.StartTimer
                }
            ) {
                Text("Start timer")
            }

            Button(
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