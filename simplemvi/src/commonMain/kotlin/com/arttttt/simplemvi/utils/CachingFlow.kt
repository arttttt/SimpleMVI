package com.arttttt.simplemvi.utils

import kotlinx.atomicfu.AtomicInt
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.atomicfu.atomic

/**
 * Represents a Flow that buffers all values when there are no subscribers.
 * [CachingFlow] emits all cached values when the first subscriber appears
 * and always drops oldest events when the buffer size is exceeded.
 *
 * @param T the type of elements contained in the flow
 * @param capacity the maximum capacity of the cache
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
public class CachingFlow<T>(
    private val capacity: Int,
) : MutableSharedFlow<T> {

    private val cache: ArrayDeque<T> = ArrayDeque(capacity)
    private val mutex: Mutex = Mutex()
    private val activeSubscribers: AtomicInt = atomic(0)

    private val _sharedFlow: MutableSharedFlow<T> = MutableSharedFlow(
        extraBufferCapacity = capacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val replayCache: List<T>
        get() = _sharedFlow.replayCache

    override val subscriptionCount: StateFlow<Int>
        get() = _sharedFlow.subscriptionCount

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        val isFirstSubscriber = activeSubscribers.incrementAndGet() == 1

        if (isFirstSubscriber) {
            val tmp = mutex.withLock {
                ArrayDeque(cache).apply {
                    cache.clear()
                }
            }

            while (tmp.isNotEmpty()) {
                collector.emit(tmp.removeFirst())
            }
        }

        try {
            _sharedFlow.collect(collector)
        } finally {
            activeSubscribers.decrementAndGet()
        }
    }

    override suspend fun emit(value: T) {
        val currentSubscribers = activeSubscribers.value
        if (currentSubscribers > 0) {
            _sharedFlow.emit(value)
        } else {
            mutex.withLock {
                if (cache.size >= capacity) {
                    cache.removeFirst()
                }
                cache.addLast(value)
            }
        }
    }

    override fun tryEmit(value: T): Boolean {
        val currentSubscribers = activeSubscribers.value
        return if (currentSubscribers > 0) {
            _sharedFlow.tryEmit(value)
        } else {
            if (mutex.tryLock()) {
                try {
                    if (cache.size >= capacity) {
                        cache.removeFirst()
                    }
                    cache.addLast(value)
                    true
                } finally {
                    mutex.unlock()
                }
            } else {
                false
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        _sharedFlow.resetReplayCache()
    }
}