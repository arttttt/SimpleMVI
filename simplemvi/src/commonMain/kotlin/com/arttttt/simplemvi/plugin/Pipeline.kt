package com.arttttt.simplemvi.plugin

/**
 * Controls the fate of an [Intent] as it flows through the [StorePlugin] chain on its way to the actor.
 *
 * The store builds one [Pipeline] per accepted intent and invokes each plugin's
 * [StorePlugin.onIntent] in registration order. Inside that callback a plugin can:
 *
 * - leave the intent untouched — it continues to the next plugin and then to the actor;
 * - call [transform] to replace the intent with a different one for downstream plugins and the actor;
 * - call [block] to drop the intent entirely — neither subsequent plugins nor the actor will see it.
 *
 * Once [block] has been called the pipeline is short-circuited and the store stops iterating plugins.
 */
public class Pipeline<Intent : Any> internal constructor(
    intent: Intent,
) {

    private var currentIntent: Intent? = intent

    /**
     * Drops the intent. Downstream plugins and the actor will not be invoked for it.
     */
    public fun block() {
        currentIntent = null
    }

    /**
     * Replaces the in-flight intent with [newIntent]. Downstream plugins and the actor will see
     * the new value instead of the original one.
     */
    public fun transform(newIntent: Intent) {
        currentIntent = newIntent
    }

    /**
     * Invokes [StorePlugin.onIntent] for this plugin with the intent that is currently in flight.
     * Internal — called by the store while iterating the plugin chain.
     */
    public fun StorePlugin<Intent, *, *>.handleIntent() {
        val intent = currentIntent ?: return

        with(this) {
            onIntent(intent)
        }
    }

    /**
     * Returns `true` while the intent is still alive (no plugin has called [block] yet).
     * Internal — used by the store to short-circuit the plugin chain.
     */
    public fun canProceed(): Boolean = currentIntent != null
}
