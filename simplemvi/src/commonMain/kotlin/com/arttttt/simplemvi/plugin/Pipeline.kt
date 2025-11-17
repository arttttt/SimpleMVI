package com.arttttt.simplemvi.plugin

public class Pipeline<Intent : Any> internal constructor(
    intent: Intent,
) {

    private var currentIntent: Intent? = intent

    public fun block() {
        currentIntent = null
    }

    public fun transform(newIntent: Intent) {
        currentIntent = newIntent
    }

    public fun StorePlugin<Intent, *, *>.handleIntent() {
        val intent = currentIntent ?: return

        with(this) {
            onIntent(intent)
        }
    }

    public fun canProceed(): Boolean = currentIntent != null
}