package com.arttttt.simplemvi.sample.timer.store

import android.util.Log
import com.arttttt.simplemvi.middleware.Middleware

class TimerMiddleware : Middleware<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> {

    private val tag = "TimerMiddleware"

    override fun onIntent(intent: TimerStore.Intent, state: TimerStore.State) {
        logV(
            """
                TimerStore received intent: $intent,
                current state: $state,
            """.trimIndent()
        )
    }

    override fun onStateChanged(oldState: TimerStore.State, newState: TimerStore.State) {
        logV(
            """
                TimerStore changed state
                old state: $oldState
                new state: $newState
            """.trimIndent()
        )
    }

    override fun onSideEffect(sideEffect: TimerStore.SideEffect, state: TimerStore.State) {
        logV(
            """
                TimerStore emitted side effect: $sideEffect
                current state: $state
            """.trimIndent()
        )
    }

    private fun logV(message: String) {
        Log.v(tag, message)
    }
}