package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.middleware.Middleware
import com.arttttt.simplemvi.utils.MainThread
import com.arttttt.simplemvi.utils.assertOnMainThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultStore<in Intent : Any, out State : Any, out SideEffect : Any>(
    initialState: State,
    private val initialIntents: List<Intent>,
    private val middlewares: List<Middleware<Intent, State, SideEffect>>,
    private val actor: Actor<Intent, State, SideEffect>,
) : Store<Intent, State, SideEffect> {

    private val _states: MutableStateFlow<State> = MutableStateFlow(initialState)

    override val states: StateFlow<State> = _states.asStateFlow()

    private val _sideEffects: MutableSharedFlow<SideEffect> = MutableSharedFlow(
        extraBufferCapacity = 1,
    )

    override val sideEffects: Flow<SideEffect> = _sideEffects.asSharedFlow()

    private var isInitialized = false

    @MainThread
    override fun init() {
        assertOnMainThread()

        if (isInitialized) return

        isInitialized = true

        actor.init(
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
    }

    @MainThread
    private fun postSideEffect(sideEffect: SideEffect) {
        assertOnMainThread()

        middlewares.forEach { it.onSideEffect(sideEffect, _states.value) }
        _sideEffects.tryEmit(sideEffect)
    }
}