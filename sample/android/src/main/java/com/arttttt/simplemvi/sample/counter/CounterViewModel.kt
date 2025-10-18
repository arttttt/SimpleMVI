package com.arttttt.simplemvi.sample.counter

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.sample.shared.store.counter.CounterStore
import com.arttttt.simplemvi.viewmodel.attachStore
import kotlinx.coroutines.Dispatchers

class CounterViewModel : ViewModel() {

    val store = CounterStore(
        coroutineContext = Dispatchers.Main.immediate,
    )

    init {
        attachStore(store)
    }
}