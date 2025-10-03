package com.arttttt.simplemvi.viewmodel

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.store.Store

/**
 * Attaches a [Store] to a [ViewModel]'s lifecycle
 *
 * This extension function:
 * 1. Calls [Store.init] to initialize the store
 * 2. Registers a cleanup callback that calls [Store.destroy] when the ViewModel is cleared
 *
 * This ensures proper store lifecycle management tied to the ViewModel's lifecycle,
 * preventing memory leaks and resource leaks.
 *
 * Example:
 * ```
 * class MyViewModel : ViewModel() {
 *     private val store = createStore(...)
 *
 *     init {
 *         attachStore(store)
 *     }
 *
 *     // Store will be automatically destroyed when ViewModel is cleared
 * }
 * ```
 *
 * @param store The [Store] to attach to this [ViewModel]
 */
public fun <Intent : Any, State : Any, SideEffect : Any> ViewModel.attachStore(
    store: Store<Intent, State, SideEffect>,
) {
    addCloseable(
        AutoCloseable {
            store.destroy()
        }
    )

    store.init()
}