package com.arttttt.simplemvi.timer

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.store.attachStore
import kotlinx.coroutines.Dispatchers

class TimerViewModel : ViewModel() {

    val store: TimerStore = TimerStore(
        coroutineContext = Dispatchers.Main.immediate,
    )

    init {
        attachStore(store)
    }
}