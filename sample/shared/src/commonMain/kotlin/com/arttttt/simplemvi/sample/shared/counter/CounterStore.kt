package com.arttttt.simplemvi.sample.shared.counter

import com.arttttt.simplemvi.actor.ActorScope
import com.arttttt.simplemvi.actor.dsl.ActorBuilder
import com.arttttt.simplemvi.actor.dsl.actorDsl
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName
import kotlin.coroutines.CoroutineContext

class CounterStore(
    coroutineContext: CoroutineContext,
) : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> by createStore(
    name = storeName<CounterStore>(),
    coroutineContext = coroutineContext,
    initialState = State(
        counter = 0,
    ),
    initialIntents = emptyList(),
    middlewares = emptyList(),
    actor = actorDsl {
            onIntent<Intent.Increment> {
                handleIncrement()
            }

            handleDecrement()

            onIntent<Intent.Reset> {
                if (state.counter == 0) {
                    sideEffect(SideEffect.CantResetCounter)
                } else {
                    reduce {
                        copy(
                            counter = 0
                        )
                    }

                    sideEffect(SideEffect.CounterReset)
                }
            }
        },
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

private fun ActorScope<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handleIncrement() {
    reduce {
        copy(
            counter = counter + 1
        )
    }

    sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
}

private fun ActorBuilder<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect>.handleDecrement() {
    onIntent<CounterStore.Intent.Decrement> {
        reduce {
            copy(
                counter = counter - 1
            )
        }

        sideEffect(CounterStore.SideEffect.CounterChanged(counter = state.counter))
    }
}