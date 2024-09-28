package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.middleware.Middleware
import com.arttttt.simplemvi.utils.CachingChannelFlow
import com.arttttt.simplemvi.utils.MainThread
import com.arttttt.simplemvi.utils.assertOnMainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

/**
 * Default [Store] implementation
 */
public class DefaultStore<in Intent : Any, out State : Any, out SideEffect : Any>(
    coroutineContext: CoroutineContext,
    initialState: State,
    private val initialIntents: List<Intent>,
    private val middlewares: List<Middleware<Intent, State, SideEffect>>,
    private val actor: Actor<Intent, State, SideEffect>,
) : Store<Intent, State, SideEffect> {

    private val _states: MutableStateFlow<State> = MutableStateFlow(initialState)

    override val state: State
        get() = _states.value

    override val states: StateFlow<State> = _states.asStateFlow()

    private val scope: CoroutineScope = CoroutineScope(coroutineContext + Job())

    private val _sideEffects: CachingChannelFlow<SideEffect> = CachingChannelFlow(
        capacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        scope = scope,
    )

    override val sideEffects: Flow<SideEffect> = _sideEffects

    private var isInitialized = false

    @MainThread
    override fun init() {
        assertOnMainThread()

        if (isInitialized) return

        isInitialized = true

        actor.init(
            scope = scope,
            getState = this::state::get,
            reduce = { block ->
                _states.update { state ->
                    block(state).also { newState ->
                        middlewares.forEach { it.onStateChanged(state, newState) }
                    }

                }
            },
            onNewIntent = this::accept,
            postSideEffect = ::postSideEffect,
        )

        initialIntents.forEach(this::accept)
    }

    @MainThread
    override fun accept(intent: Intent) {
        assertOnMainThread()

        middlewares.forEach { it.onIntent(intent, _states.value) }
        actor.onIntent(intent)
    }

    @MainThread
    override fun destroy() {
        assertOnMainThread()

        actor.destroy()
        scope.cancel()
    }

    @MainThread
    private fun postSideEffect(sideEffect: SideEffect) {
        assertOnMainThread()

        middlewares.forEach { it.onSideEffect(sideEffect, _states.value) }
        _sideEffects.tryEmit(sideEffect)
    }
}