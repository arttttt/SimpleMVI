package com.arttttt.simplemvi.sample.shared.timer

import com.arttttt.simplemvi.middleware.Middleware

class TimerMiddleware : Middleware<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> {

    private val tag = "TimerMiddleware"

    override fun onInit(state: TimerStore.State) {
        logV(
            """
                TimerStore initialized
            """.trimIndent()
        )
    }

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

    override fun onDestroy(state: TimerStore.State) {
        logV(
            """
                TimerStore destroyed
            """.trimIndent()
        )
    }

    private fun logV(message: String) {
        println("$tag: $message")
    }
}