package com.arttttt.simplemvi.logging.logger.com.arttttt.simplemvi.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

public class CStateFlow<T : Any>(
    private val source: StateFlow<T>,
) {

    public val value: T
        get() = source.value

    public fun subscribe(
        onEach: (T) -> Unit,
        onCompletion: (error: Throwable?) -> Unit,
    ): CFlowSubscription {
        val scope = CoroutineScope(Job() + Dispatchers.Main.immediate)

        source
            .onEach { value -> onEach(value) }
            .catch { error -> onCompletion(error) }
            .onCompletion { onCompletion(null) }
            .launchIn(scope)

        return CFlowSubscription { scope.cancel() }
    }
}