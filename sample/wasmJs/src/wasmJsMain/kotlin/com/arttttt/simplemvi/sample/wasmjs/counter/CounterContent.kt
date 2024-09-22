package com.arttttt.simplemvi.sample.wasmjs.counter

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arttttt.simplemvi.sample.shared.counter.CounterStore
import com.arttttt.simplemvi.store.plus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterContent() {
    val store = remember { CounterStore(Dispatchers.Main.immediate) }
    val snackbarState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val state by store.states.collectAsState()

        LaunchedEffect(Unit) {
            store
                .sideEffects
                .onEach { sideEffect ->
                    when (sideEffect) {
                        is CounterStore.SideEffect.CounterChanged -> {
                            snackbarState.showSnackbar("Counter changed to ${sideEffect.counter}")
                        }
                        is CounterStore.SideEffect.CantResetCounter -> {
                            snackbarState.showSnackbar("Can't reset counter")
                        }
                        is CounterStore.SideEffect.CounterReset -> {
                            snackbarState.showSnackbar("Counter reset")
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
                        store + CounterStore.Intent.Increment
                    }
                ) {
                    Text("Increment")
                }

                Button(
                    onClick = {
                        store + CounterStore.Intent.Decrement
                    }
                ) {
                    Text("Decrement")
                }

                Button(
                    onClick = {
                        store + CounterStore.Intent.Reset
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

    SnackbarHost(snackbarState)
}