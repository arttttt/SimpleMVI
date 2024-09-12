package com.arttttt.simplemvi.utils

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.store.Store

fun <Intent : Any, State : Any, SideEffect : Any> ViewModel.attachStore(
    store: Store<Intent, State, SideEffect>,
) {
    addCloseable(
        AutoCloseable {
            store.destroy()
        }
    )

    store.init()
}