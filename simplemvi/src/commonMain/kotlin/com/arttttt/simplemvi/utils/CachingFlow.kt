package com.arttttt.simplemvi.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents a Flow that buffers all values when there are no subscribers
 * [CachingFlow] emits all cached values when the first subscriber appears
 * [CachingFlow] always drops oldest events when the buffer size exceeded
 *
 * @param T the type of elements contained in the flow.
 * @param capacity the maximum capacity of the cache.
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
public class CachingFlow<T>(
    private val capacity: Int,
) : MutableSharedFlow<T> {

    private val cache: ArrayDeque<T> = ArrayDeque()
    private val mutex: Mutex = Mutex()
    private var activeSubscribers: Int = 0

    private val _sharedFlow: MutableSharedFlow<T> = MutableSharedFlow(
        extraBufferCapacity = capacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val replayCache: List<T>
        get() = _sharedFlow.replayCache

    override val subscriptionCount: StateFlow<Int>
        get() = _sharedFlow.subscriptionCount

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

    private fun emitInternal(value: T): Boolean {
        return if (activeSubscribers > 0) {
            _sharedFlow.tryEmit(value)
        } else {
            if (cache.size >= capacity) {
                cache.removeFirst()
            }
            cache.addLast(value)
            true
        }
    }
}