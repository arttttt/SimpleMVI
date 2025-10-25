package com.arttttt.simplemvi.sample.shared.store.timer

import com.arttttt.simplemvi.annotations.TCAFeature
import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import kotlinx.serialization.Serializable
import kotlin.coroutines.CoroutineContext

@TCAFeature
class TimerStore(
    coroutineContext: CoroutineContext,
    plugins: List<StorePlugin<Intent, State, SideEffect>>,
) : Store<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> by createStore(
    name = null,
    coroutineContext = coroutineContext,
    initialState = State(
        isTimerRunning = false,
        value = 0,
    ),
    initialIntents = emptyList(),
    middlewares = listOf(
        TimerMiddleware(),
    ),
    plugins = plugins,
    actor = TimerStoreActor(),
) {

    sealed interface Intent {

        data object StartTimer : Intent
        data object StopTimer : Intent
        data object ResetTimer : Intent
    }

    @Serializable
    data class State(
        val isTimerRunning: Boolean,
        val value: Int,
    )

    sealed interface SideEffect
}