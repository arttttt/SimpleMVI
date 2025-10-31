package com.arttttt.simplemvi.sample.counter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.sample.shared.store.counter.CounterStore
import com.arttttt.simplemvi.savedstate.saveStatePlugin
import com.arttttt.simplemvi.savedstate.toPluginSavedStateHandle
import com.arttttt.simplemvi.viewmodel.attachStore
import kotlinx.coroutines.Dispatchers

class CounterViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val store = CounterStore(
        coroutineContext = Dispatchers.Main.immediate,
        plugins = listOf(
            saveStatePlugin(
                handle = savedStateHandle.toPluginSavedStateHandle(),
            )
        )
    )

    init {
        attachStore(store)
    }
}