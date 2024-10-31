@file:OptIn(ExperimentalUuidApi::class)

package com.arttttt.simplemvi.timetravel

import com.arttttt.simplemvi.logging.logger.logV
import com.arttttt.simplemvi.store.Store
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

public sealed interface TimelineEvent<Intent : Any, State : Any, SideEffect : Any> {
    public val id: Uuid
    public val timestamp: Long
    public val state: State

    public data class IntentReceived<Intent : Any, State : Any, SideEffect : Any>(
        override val id: Uuid = Uuid.random(),
        override val timestamp: Long = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds,
        override val state: State,
        public val intent: Intent
    ) : TimelineEvent<Intent, State, SideEffect>

    public data class StateChanged<Intent : Any, State : Any, SideEffect : Any>(
        override val id: Uuid = Uuid.random(),
        override val timestamp: Long = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds,
        override val state: State,
        public val previousState: State
    ) : TimelineEvent<Intent, State, SideEffect>

    public data class SideEffectEmitted<Intent : Any, State : Any, SideEffect : Any>(
        override val id: Uuid = Uuid.random(),
        override val timestamp: Long = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds,
        override val state: State,
        public val sideEffect: SideEffect
    ) : TimelineEvent<Intent, State, SideEffect>
}

public class TimeTravelStore<Intent : Any, State : Any, SideEffect : Any>(
    private val delegate: Store<Intent, State, SideEffect>
) : Store<Intent, State, SideEffect> {

    private val timelineEvents = mutableListOf<TimelineEvent<Intent, State, SideEffect>>()

    private val _currentPosition: MutableStateFlow<Int> = MutableStateFlow(0)
    public val currentPosition: StateFlow<Int> = _currentPosition

    public var isTimeTravel: Boolean = false
        private set

    private val _timeTravelState: MutableStateFlow<State> = MutableStateFlow(delegate.state)
    private val _states: MutableStateFlow<State> = MutableStateFlow(delegate.state)

    override val state: State
        get() = if (isTimeTravel) {
            _timeTravelState.value
        } else {
            delegate.state
        }

    override val states: StateFlow<State> = _states

    override val sideEffects: Flow<SideEffect> = delegate.sideEffects

    override fun init() {
        delegate.init()
        _states.value = delegate.state
    }

    override fun accept(intent: Intent) {
        if (!isTimeTravel) {
            recordEvent(
                TimelineEvent.IntentReceived(
                    state = state,
                    intent = intent
                )
            )
            delegate.accept(intent)
            _states.value = delegate.state
        }
    }

    override fun destroy() {
        delegate.destroy()
    }

    public fun moveToPosition(position: Int) {
        if (!isTimeTravel) {
            isTimeTravel = true
        }

        logV(
            "test",
            """
                position: $position
                events count: ${timelineEvents.size}
                events: $timelineEvents
            """.trimIndent()
        )

        if (position in 0 until timelineEvents.size) {
            _currentPosition.value = position

            timelineEvents.getOrNull(position)?.state?.let { state ->
                _timeTravelState.value = state
                _states.value = state
            }
        }
    }

    public fun stepForward() {
        moveToPosition(currentPosition.value + 1)
    }

    public fun stepBackward() {
        moveToPosition(currentPosition.value - 1)
    }

    public fun resumeLive() {
        isTimeTravel = false
        _states.value = delegate.state
        _currentPosition.value = timelineEvents.lastIndex
    }

    public fun clear() {
        timelineEvents.clear()
        _currentPosition.value = 0
        resumeLive()
    }

    public fun getEvents(): List<TimelineEvent<Intent, State, SideEffect>> = timelineEvents.toList()

    private fun recordEvent(event: TimelineEvent<Intent, State, SideEffect>) {
        timelineEvents.add(event)

        logV(
            "test",
            """
                event: $event
            """.trimIndent()
        )

        if (!isTimeTravel) {
            _currentPosition.update { position ->
                position + 1
            }
        }
    }
}

public fun <Intent : Any, State : Any, SideEffect : Any> Store<Intent, State, SideEffect>.enableTimeTravel(): TimeTravelStore<Intent, State, SideEffect> {
    return TimeTravelStore(this)
}