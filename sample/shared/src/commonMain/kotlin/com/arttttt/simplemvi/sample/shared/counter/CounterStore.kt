package com.arttttt.simplemvi.sample.shared.counter

import com.arttttt.simplemvi.actor.dsl.delegatedActor
import com.arttttt.simplemvi.annotations.DelegatedStore
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName
import kotlin.coroutines.CoroutineContext

@DelegatedStore
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
    actor = delegatedActor(
        intentHandlers = listOf(
            IncrementHandler(),
            DecrementHandler(),
            ResetHandler(),
        ),
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