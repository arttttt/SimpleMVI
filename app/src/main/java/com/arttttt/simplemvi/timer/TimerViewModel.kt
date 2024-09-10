package com.arttttt.simplemvi.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    val store: TimerStore = TimerStore(
        coroutineContext = Dispatchers.Main.immediate,
    )

    init {
        viewModelScope
            .launch { store.init() }
            .invokeOnCompletion { store.destroy() }
    }
}