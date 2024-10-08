package com.arttttt.simplemvi.viewmodel

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.store.Store

/**
 * This function attaches the [Store] to the scope of the [ViewModel]
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