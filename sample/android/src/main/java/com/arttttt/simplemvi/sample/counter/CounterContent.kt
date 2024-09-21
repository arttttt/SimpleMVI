package com.arttttt.simplemvi.sample.counter

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arttttt.simplemvi.store.plus
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun CounterContent() {
    val viewModel: CounterViewModel = viewModel()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val state by viewModel.store.states.collectAsState()

        val context = LocalContext.current

        LaunchedEffect(Unit) {
            viewModel
                .store
                .sideEffects
                .onEach { sideEffect ->
                    when (sideEffect) {
                        is CounterStore.SideEffect.CounterChanged -> {
                            Toast.makeText(context, "Counter changed to ${sideEffect.counter}", Toast.LENGTH_SHORT).show()
                        }
                        is CounterStore.SideEffect.CantResetCounter -> {
                            Toast.makeText(context, "Can't reset counter", Toast.LENGTH_SHORT).show()
                        }
                        is CounterStore.SideEffect.CounterReset -> {
                            Toast.makeText(context, "Counter reset", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .launchIn(this)
        }

        TopAppBar(
            title = {
                Text("Counter")
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
                    onClick = {
                        viewModel.store + CounterStore.Intent.Increment
                    }
                ) {
                    Text("Increment")
                }

                Button(
                    onClick = {
                        viewModel.store + CounterStore.Intent.Decrement
                    }
                ) {
                    Text("Decrement")
                }

                Button(
                    onClick = {
                        viewModel.store + CounterStore.Intent.Reset
                    }
                ) {
                    Text("Reset")
                }
            }

            Text(
                text = "counter: ${state.counter}"
            )
        }
    }
}