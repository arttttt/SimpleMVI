package com.arttttt.simplemvi.sample.timer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.sample.shared.store.timer.TimerStore
import com.arttttt.simplemvi.savedstate.saveStatePlugin
import com.arttttt.simplemvi.savedstate.toPluginSavedStateHandle
import com.arttttt.simplemvi.viewmodel.attachStore
import kotlinx.coroutines.Dispatchers

class TimerViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val store: TimerStore = TimerStore(
        coroutineContext = Dispatchers.Main.immediate,
        plugins = listOf(
            saveStatePlugin(
                handle = savedStateHandle.toPluginSavedStateHandle(),
            )
        ),
    )

    init {
        attachStore(store)
    }
}