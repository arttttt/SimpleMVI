package com.arttttt.simplemvi.sample.shared.store.timer

import com.arttttt.simplemvi.annotations.TCAFeature
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import kotlin.coroutines.CoroutineContext

@TCAFeature
class TimerStore(
    coroutineContext: CoroutineContext,
) : Store<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> by createStore(
    name = null,
    coroutineContext = coroutineContext,
    initialState = State(
        isTimerRunning = false,
        value = 0,
    ),
    initialIntents = emptyList(),
    plugins = listOf(
        TimerPlugin(),
    ),
    actor = TimerStoreActor(),
) {

    sealed interface Intent {

        data object StartTimer : Intent
        data object StopTimer : Intent
        data object ResetTimer : Intent
    }

    data class State(
        val isTimerRunning: Boolean,
        val value: Int,
    )

    sealed interface SideEffect
}