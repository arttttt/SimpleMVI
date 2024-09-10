package com.arttttt.simplemvi.counter

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.store.attachStore
import kotlinx.coroutines.Dispatchers

class CounterViewModel : ViewModel() {

    val store = CounterStore(
        coroutineContext = Dispatchers.Main.immediate,
    )

    init {
        attachStore(store)
    }
}