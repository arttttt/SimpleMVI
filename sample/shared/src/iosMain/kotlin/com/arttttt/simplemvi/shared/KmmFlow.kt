package com.arttttt.simplemvi.shared

import com.arttttt.simplemvi.store.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*

fun interface KmmSubscription {
    fun unsubscribe()
}

class KmmFlow<T>(private val source: StateFlow<T>) : StateFlow<T> by source {

    fun subscribe(
        onEach: (T) -> Unit,
        onCompletion: (Throwable?) -> Unit,
    ): KmmSubscription {
        val scope = CoroutineScope(Job() + Dispatchers.Main.immediate)
        source
            .onEach { onEach(it) }
            .catch { onCompletion(it) }
            .onCompletion { onCompletion(null) }
            .launchIn(scope)
        return KmmSubscription { scope.cancel() }
    }
}

fun <T : Any> Store<*, T, *>.kmmStates(): KmmFlow<T> {
    return KmmFlow(states)
}