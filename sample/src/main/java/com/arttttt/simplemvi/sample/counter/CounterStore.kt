package com.arttttt.simplemvi.sample.counter

import android.util.Log
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.dsl.ActorBuilder
import com.arttttt.simplemvi.logging.loggingActor
import com.arttttt.simplemvi.utils.actorDsl
import com.arttttt.simplemvi.utils.createStore
import kotlin.coroutines.CoroutineContext

class CounterStore(
    coroutineContext: CoroutineContext,
) : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> by createStore(
    initialState = State(
        counter = 0,
    ),
    initialIntents = emptyList(),
    actor = loggingActor(
        name = "CounterStore",
        logger = { message -> Log.e("CounterStore", message) },
        delegate = actorDsl(
            coroutineContext = coroutineContext,
        ) {
            onIntent<Intent.Increment> { intent ->
                handleIncrement(intent)
            }

            handleDecrement()

            onIntent<Intent.Reset> {
                if (state.counter == 0) {
                    sideEffect(SideEffect.CantResetCounter)
                } else {
                    reduce { state ->
                        state.copy(
                            counter = 0
                        )
                    }

                    sideEffect(SideEffect.CounterReset)
                }
            }
        },
    )
) {

    sealed interface Intent {

        data object Increment : Intent
        data object Decrement : Intent
        data object Reset : Intent
    }

    data class State(
        val counter: Int,
    )

    sealed interface SideEffect {

        data class CounterChanged(val counter: Int) : SideEffect
        data object CantResetCounter : SideEffect
        data object CounterReset : SideEffect
    }
}

private fun ActorScope<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handleIncrement(
    intent: CounterStore.Intent.Increment,
) {
    reduce { state ->
        state.copy(
            counter = state.counter + 1
        )
    }

    sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
}

private fun ActorBuilder<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handleDecrement() {
    onIntent<CounterStore.Intent.Decrement> {
        reduce { state ->
            state.copy(
                counter = state.counter - 1
            )
        }

        sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
    }
}