package com.arttttt.simplemvi.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

/**
 * Represents a Flow that buffers all values when there are no subscribers.
 * [CachingChannelFlow] emits all cached values when the first subscriber appears.
 *
 * @param T the type of elements contained in the flow.
 * @param coroutineContext in which context [CachingChannelFlow] works.
 * @param onBufferOverflow configures an action on buffer overflow.
 * @param capacity the maximum capacity of the cache.
 */
public class CachingChannelFlow<T>(
    coroutineContext: CoroutineContext,
    onBufferOverflow: BufferOverflow,
    private val capacity: Int,
) : MutableSharedFlow<T> {

    private val channel: Channel<T> = Channel(
        capacity = capacity,
        onBufferOverflow = onBufferOverflow,
    )
    private val cache: ArrayDeque<T> = ArrayDeque()
    private val mutex: Mutex = Mutex()
    private var activeSubscribers: Int = 0

    private val scope: CoroutineScope = CoroutineScope(coroutineContext + Job())

    private val _sharedFlow: MutableSharedFlow<T> = MutableSharedFlow(
        extraBufferCapacity = capacity,
        onBufferOverflow = onBufferOverflow,
    )

    override val replayCache: List<T>
        get() = _sharedFlow.replayCache

    override val subscriptionCount: StateFlow<Int>
        get() = _sharedFlow.subscriptionCount

    init {
        scope.launch {
            for (value in channel) {
                _sharedFlow.emit(value)
            }
        }
    }

    override suspend fun emit(value: T) {
        mutex.withLock {
            emitInternal(value)
        }
    }

    override fun tryEmit(value: T): Boolean {
        return if (mutex.tryLock()) {
            try {
                emitInternal(value)
            } finally {
                mutex.unlock()
            }
        } else {
            false
        }
    }

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        mutex.withLock {
            activeSubscribers += 1
            if (activeSubscribers == 1) {
                while (cache.isNotEmpty()) {
                    collector.emit(cache.removeFirst())
                }
            }
        }
        try {
            _sharedFlow.collect(collector)
        } finally {
            mutex.withLock {
                activeSubscribers -= 1
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        _sharedFlow.resetReplayCache()
    }

    public fun close() {
        channel.close()
        scope.cancel()
    }

    private fun emitInternal(value: T): Boolean {
        return if (activeSubscribers > 0) {
            channel.trySend(value).isSuccess
        } else {
            if (cache.size >= capacity) {
                cache.removeFirst()
            }
            cache.addLast(value)
            true
        }
    }
}