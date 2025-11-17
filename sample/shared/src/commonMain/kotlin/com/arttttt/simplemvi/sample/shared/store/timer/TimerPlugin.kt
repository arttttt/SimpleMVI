package com.arttttt.simplemvi.sample.shared.store.timer

import com.arttttt.simplemvi.plugin.Pipeline
import com.arttttt.simplemvi.plugin.StorePlugin
import kotlin.properties.Delegates

class TimerPlugin : StorePlugin<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> {

    private val tag = "TimerLoggingPlugin"

    private var context: StorePlugin.Context<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect> by Delegates.notNull()


    override fun onInit(context: StorePlugin.Context<TimerStore.Intent, TimerStore.State, TimerStore.SideEffect>) {
        this.context = context

        logV(
            """
                TimerStore initialized
            """.trimIndent()
        )
    }

    override fun Pipeline<TimerStore.Intent>.onIntent(intent: TimerStore.Intent) {
        logV(
            """
                TimerStore received intent: $intent,
                current state: ${context.state},
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

    override fun onSideEffect(sideEffect: TimerStore.SideEffect) {
        logV(
            """
                TimerStore emitted side effect: $sideEffect
                current state: ${context.state}
            """.trimIndent()
        )
    }

    override fun onDestroy() {
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