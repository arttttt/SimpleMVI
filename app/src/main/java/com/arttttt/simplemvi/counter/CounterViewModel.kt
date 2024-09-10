package com.arttttt.simplemvi.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CounterViewModel : ViewModel() {

    val store = CounterStore(
        coroutineContext = Dispatchers.Main.immediate,
    )

    init {
        viewModelScope
            .launch { store.init() }
            .invokeOnCompletion { store.destroy() }
    }
}