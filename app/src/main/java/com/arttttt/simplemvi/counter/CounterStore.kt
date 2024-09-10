package com.arttttt.simplemvi.counter

import android.util.Log
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.actor.ActorScope
import com.arttttt.simplemvi.store.actor.dsl.ActorBuilder
import com.arttttt.simplemvi.store.actorDsl
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.loggingActor
import kotlin.coroutines.CoroutineContext

class CounterStore(
    coroutineContext: CoroutineContext,
) : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> by createStore(
    initialState = State(
        counter = 0,
    ),
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
                if (getState().counter == 0) {
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

    sideEffect(CounterStore.SideEffect.CounterChanged(counter = getState().counter))
}

private fun ActorBuilder<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handleDecrement() {
    onIntent<CounterStore.Intent.Decrement> {
        reduce { state ->
            state.copy(
                counter = state.counter - 1
            )
        }

        sideEffect(CounterStore.SideEffect.CounterChanged(counter = getState().counter))
    }
}