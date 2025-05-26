package com.arttttt.simplemvi.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.state.KotlinSerializationStateSerializer
import com.arttttt.simplemvi.state.StateSaver
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.StoreName
import com.arttttt.simplemvi.viewmodel.state.SavedStateHandleStateSaver
import kotlinx.serialization.serializer

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

public inline fun <reified T : Any> SavedStateHandle.stateSaverFactory(
    noinline transform: (state: T) -> T = { it },
): (StoreName) -> StateSaver<T> {
    return { name ->
        SavedStateHandleStateSaver(
            savedStateHandle = this,
            serializer = KotlinSerializationStateSerializer(
                serializer = serializer(),
            ),
            key = name.name,
            transform = transform,
        )
    }
}