package com.arttttt.simplemvi.sample.timer

import android.util.Log
import com.arttttt.simplemvi.middleware.Middleware

class TimerMiddleware : Middleware<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> {

    private val tag = "TimerMiddleware"

    override fun onIntent(intent: TimerStore.Intent, state: TimerStore.State) {
        logE(
            """
                TimerStore received intent: $intent,
                current state: $state,
            """.trimIndent()
        )
    }

    override fun onStateChanged(oldState: TimerStore.State, newState: TimerStore.State) {
        logE(
            """
                TimerStore changed state
                old state: $oldState
                new state: $newState
            """.trimIndent()
        )
    }

    override fun onSideEffect(sideEffect: TimerStore.SideEffect, state: TimerStore.State) {
        logE(
            """
                TimerStore emitted side effect: $sideEffect
                current state: $state
            """.trimIndent()
        )
    }

    private fun logE(message: String) {
        Log.e(tag, message)
    }
}