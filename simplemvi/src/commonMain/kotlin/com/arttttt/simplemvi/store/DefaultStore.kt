package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.config.simpleMVIConfig
import com.arttttt.simplemvi.middleware.Middleware
import com.arttttt.simplemvi.utils.CachingFlow
import com.arttttt.simplemvi.utils.MainThread
import com.arttttt.simplemvi.utils.assertOnMainThread
import com.arttttt.simplemvi.utils.exceptions.StoreIsAlreadyDestroyedException
import com.arttttt.simplemvi.utils.exceptions.StoreIsNotInitializedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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

    private val _sideEffects: CachingFlow<SideEffect> = CachingFlow(
        capacity = 64,
    )

    override val sideEffects: Flow<SideEffect> = _sideEffects

    private var isInitialized: Boolean = false
    private var isDestroyed: Boolean = false

    @MainThread
    override fun init() {
        if (isInitialized) return

        assertOnMainThread()

        isInitialized = true

        middlewares.forEach { it.onInit(_states.value) }

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
        if (!isInitialized) {
            val message = "Attempting to use an uninitialized Store"
            if (simpleMVIConfig.strictMode) {
                throw StoreIsNotInitializedException()
            } else {
                simpleMVIConfig.logger?.log(message)
                return
            }
        }

        if (isDestroyed) {
            val message = "Attempting to use a destroyed Store"
            if (simpleMVIConfig.strictMode) {
                throw StoreIsAlreadyDestroyedException()
            } else {
                simpleMVIConfig.logger?.log(message)
                return
            }
        }

        assertOnMainThread()

        middlewares.forEach { it.onIntent(intent, _states.value) }
        actor.onIntent(intent)
    }

    @MainThread
    override fun destroy() {
        if (isDestroyed) return

        isDestroyed = true

        assertOnMainThread()

        middlewares.forEach { it.onDestroy(_states.value) }

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