package com.arttttt.simplemvi

import com.arttttt.simplemvi.actor.Actor
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultStore<in Intent : Any, out State : Any, out SideEffect : Any>(
    initialState: State,
    private val actor: Actor<Intent, State, SideEffect>,
) : Store<Intent, State, SideEffect> {

    private val _states: MutableStateFlow<State> = MutableStateFlow(initialState)

    override val states: StateFlow<State> = _states.asStateFlow()

    private val _sideEffects: MutableSharedFlow<SideEffect> = MutableSharedFlow(
        extraBufferCapacity = 1,
    )

    override val sideEffects: Flow<SideEffect> = _sideEffects.asSharedFlow()

    private val isInitialized = atomic(false)

    override fun init() {
        if (isInitialized.value) return

        isInitialized.value = true

        actor.init(
            getState = this::state::get,
            reduce = { block ->
                _states.update { state ->
                    block(state)
                }
            },
            onNewIntent = this::accept,
            postSideEffect = ::postSideEffect,
        )
    }

    override fun accept(intent: Intent) {
        actor.onIntent(intent)
    }

    override fun destroy() {
        actor.destroy()
    }

    private fun postSideEffect(sideEffect: SideEffect) {
        _sideEffects.tryEmit(sideEffect)
    }
}