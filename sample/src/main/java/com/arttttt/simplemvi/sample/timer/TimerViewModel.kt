package com.arttttt.simplemvi.sample.timer

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.sample.timer.store.TimerStore
import com.arttttt.simplemvi.viewmodel.attachStore
import kotlinx.coroutines.Dispatchers

class TimerViewModel : ViewModel() {

    val store: TimerStore = TimerStore(
        coroutineContext = Dispatchers.Main.immediate,
    )

    init {
        attachStore(store)
    }
}