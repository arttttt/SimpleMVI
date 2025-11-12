package com.arttttt.simplemvi.store

import com.arttttt.simplemvi.actor.Actor
import com.arttttt.simplemvi.config.simpleMVIConfig
import com.arttttt.simplemvi.plugin.PluginsOwner
import com.arttttt.simplemvi.plugin.StorePlugin
import com.arttttt.simplemvi.utils.CachingFlow
import com.arttttt.simplemvi.utils.exceptions.StoreIsAlreadyDestroyedException
import com.arttttt.simplemvi.utils.exceptions.StoreIsNotInitializedException
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Default [Store] implementation
 *
 * This class provides the core functionality for managing state, processing intents,
 * and emitting side effects.
 *
 * The implementation ensures:
 * - Thread-safe state updates using [MutableStateFlow]
 * - Proper initialization and destruction lifecycle
 * - Intent processing through the [Actor]
 * - State change notifications to middleware
 * - Side effect caching when there are no active collectors
 *
 * @param coroutineContext [CoroutineContext] for the [Store]'s [CoroutineScope]
 * @param initialState The initial [State] value
 * @param initialIntents List of [Intent]s to be processed after initialization
 * @param plugins List of [Plugin] instances to extend functionality
 * @param actor The [Actor] responsible for business logic
 *
 * @see Store
 * @see Actor
 * @see StorePlugin
 */
public class DefaultStore<Intent : Any, State : Any, SideEffect : Any>(
    coroutineContext: CoroutineContext,
    initialState: State,
    override val plugins: List<StorePlugin<Intent, State, SideEffect>>,
    private val initialIntents: List<Intent>,
    private val actor: Actor<Intent, State, SideEffect>,
) : Store<Intent, State, SideEffect>{

    private val _states: MutableStateFlow<State> = MutableStateFlow(
        plugins.fold(initialState) { state, plugin ->
            plugin.provideInitialState(state)
        }
    )

    override val state: State
        get() = _states.value

    override val states: StateFlow<State> = _states.asStateFlow()

    private val scope: CoroutineScope = CoroutineScope(coroutineContext + Job())

    private val _sideEffects: CachingFlow<SideEffect> = CachingFlow(
        capacity = 64,
    )

    override val sideEffects: Flow<SideEffect> = _sideEffects

    private val isInitialized: AtomicBoolean = atomic(false)
    private val isDestroyed: AtomicBoolean = atomic(false)

    override fun init() {
        if (isInitialized.getAndSet(true)) return

        val context = StorePlugin.Context<Intent, State, SideEffect>(
            scope = scope,
            sendIntent = this@DefaultStore::accept,
            setState = { newState ->
                val oldState = _states.value
                _states.value = newState
                plugins.forEach { it.onStateChanged(oldState, newState) }
            },
            sendSideEffect = this::postSideEffect,
            getState = this@DefaultStore::state::get,
        )
        plugins.forEach { plugin -> plugin.onInit(context) }

        actor.init(
            scope = scope,
            getState = this::state::get,
            reduce = { block ->
                _states.update { state ->
                    block(state).also { newState ->
                        plugins.forEach { plugin -> plugin.onStateChanged(state, newState) }
                    }
                }
            },
            onNewIntent = this::accept,
            postSideEffect = ::postSideEffect,
        )

        initialIntents.forEach(this::accept)
    }

    override fun accept(intent: Intent) {
        if (!isInitialized.value) {
            val message = "Attempting to use an uninitialized Store"
            if (simpleMVIConfig.strictMode) {
                throw StoreIsNotInitializedException()
            } else {
                simpleMVIConfig.logger?.log(message)
                return
            }
        }

        if (isDestroyed.value) {
            val message = "Attempting to use a destroyed Store"
            if (simpleMVIConfig.strictMode) {
                throw StoreIsAlreadyDestroyedException()
            } else {
                simpleMVIConfig.logger?.log(message)
                return
            }
        }

        plugins.forEach { plugin -> plugin.onIntent(intent) }
        actor.onIntent(intent)
    }

    override fun destroy() {
        if (isDestroyed.getAndSet(true)) return

        plugins.forEach { plugin -> plugin.onDestroy() }

        actor.destroy()
        scope.cancel()
    }

    private fun postSideEffect(sideEffect: SideEffect) {
        plugins.forEach { plugin -> plugin.onSideEffect(sideEffect) }
        scope.launch {
            _sideEffects.emit(sideEffect)
        }
    }
}