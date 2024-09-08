package com.arttttt.simplemvi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arttttt.simplemvi.store.DefaultStore
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.defaultActor
import com.arttttt.simplemvi.store.loggingActor
import com.arttttt.simplemvi.store.plus
import com.arttttt.simplemvi.ui.theme.SimpleMVITheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface CounterStore : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> {

    sealed interface Intent {

        data object Increment : Intent
        data object Decrement : Intent
    }

    data class State(
        val counter: Int,
    )

    sealed interface SideEffect {

        data class CounterChanged(val counter: Int) : SideEffect
    }

}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = createStore<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>(
            initialState = CounterStore.State(
                counter = 0
            ),
            actor = loggingActor(
                name = "CounterStore",
                logger = { message -> Log.e("CounterStore", message) },
                delegate = defaultActor(
                    coroutineContext = Dispatchers.Main.immediate
                ) { intent ->
                    when (intent) {
                        is CounterStore.Intent.Increment -> {
                            reduce {
                                copy(
                                    counter = counter + 1
                                )
                            }

                            sideEffect(CounterStore.SideEffect.CounterChanged(counter = getState().counter))
                        }
                        is CounterStore.Intent.Decrement -> {
                            reduce {
                                copy(
                                    counter = counter - 1
                                )
                            }

                            sideEffect(CounterStore.SideEffect.CounterChanged(counter = getState().counter))
                        }
                    }
                }
            )
        )

        store.init()

        setContent {
            SimpleMVITheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val state by store.states.collectAsState()

                    val context = LocalContext.current

                    LaunchedEffect(Unit) {
                        store
                            .sideEffects
                            .onEach { sideEffect ->
                                when (sideEffect) {
                                    is CounterStore.SideEffect.CounterChanged -> {
                                        Toast.makeText(context, "Counter changed to ${sideEffect.counter}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .launchIn(this)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    }

                    Text(
                        text = "counter: ${state.counter}"
                    )
                }
            }
        }
    }
}