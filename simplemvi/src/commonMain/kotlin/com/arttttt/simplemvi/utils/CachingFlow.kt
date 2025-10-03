package com.arttttt.simplemvi.utils

import kotlinx.atomicfu.AtomicInt
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.atomicfu.atomic

/**
 * A [Flow] implementation that buffers values when there are no active collectors
 *
 * This flow behaves differently depending on whether there are active collectors:
 * - **No collectors**: Values are cached in memory up to the specified [capacity]
 * - **Has collectors**: Values are emitted immediately without caching
 *
 * When the first collector subscribes, all cached values are emitted first,
 * followed by new values as they arrive.
 *
 * Buffer overflow behavior:
 * - When the cache reaches [capacity], the oldest values are dropped
 * - Uses [BufferOverflow.DROP_OLDEST] strategy
 *
 * Thread-safety:
 * - Internally uses [Mutex] for thread-safe access to the cache
 * - Safe to emit from multiple coroutines
 *
 * @param T The type of elements contained in the flow
 * @param capacity The maximum number of elements to cache (must be > 0)
 *
 * @see MutableSharedFlow
 */
@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
public class CachingFlow<T>(
    private val capacity: Int,
) : MutableSharedFlow<T> {

    /**
     * Cached values waiting to be emitted to the first subscriber
     */
    private val cache: ArrayDeque<T> = ArrayDeque(capacity)

    /**
     * Mutex for protecting access to the cache
     */
    private val mutex: Mutex = Mutex()

    /**
     * Counter tracking the number of active subscribers
     */
    private val activeSubscribers: AtomicInt = atomic(0)

    /**
     * Underlying shared flow used when there are active subscribers
     */
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