package com.arttttt.simplemvi

import androidx.lifecycle.ViewModel

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