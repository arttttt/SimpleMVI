package com.arttttt.simplemvi.sample.counter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.sample.shared.counter.CounterStore
import com.arttttt.simplemvi.viewmodel.attachStore
import com.arttttt.simplemvi.viewmodel.stateSaverFactory
import kotlinx.coroutines.Dispatchers

class CounterViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val store = CounterStore(
        coroutineContext = Dispatchers.Main.immediate,
        stateSaverFactory = savedStateHandle.stateSaverFactory(),
    )

    init {
        attachStore(store)
    }
}