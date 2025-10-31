package com.arttttt.simplemvi.sample.shared.store.counter

import com.arttttt.simplemvi.actor.dsl.delegatedActor
import com.arttttt.simplemvi.annotations.DelegatedStore
import com.arttttt.simplemvi.annotations.TCAFeature
import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName
import kotlinx.serialization.Serializable
import kotlin.coroutines.CoroutineContext

@TCAFeature
@DelegatedStore
class CounterStore(
    plugins: List<StorePlugin<Intent, State, SideEffect>>,
    coroutineContext: CoroutineContext,
) : Store<CounterStore.Intent, CounterStore.State, CounterStore.SideEffect> by createStore(
    name = storeName<CounterStore>(),
    coroutineContext = coroutineContext,
    initialState = State(
        counter = 0,
    ),
    initialIntents = emptyList(),
    middlewares = emptyList(),
    plugins = plugins,
    actor = delegatedActor(
        intentHandlers = listOf(
            incrementIntentHandler(),
            decrementIntentHandler(),
            resetIntentHandler(),
        ),
    )
) {

    sealed interface Intent {

        data object Increment : Intent
        data object Decrement : Intent
        data object Reset : Intent
    }

    @Serializable
    data class State(
        val counter: Int,
    )

    sealed interface SideEffect {

        data class CounterChanged(val counter: Int) : SideEffect
        data object CantResetCounter : SideEffect
        data object CounterReset : SideEffect
    }
}