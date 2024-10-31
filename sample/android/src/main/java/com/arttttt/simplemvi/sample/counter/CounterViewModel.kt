package com.arttttt.simplemvi.sample.counter

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.sample.shared.counter.CounterStore
import com.arttttt.simplemvi.timetravel.TimeTravelStore
import com.arttttt.simplemvi.viewmodel.attachStore
import kotlinx.coroutines.Dispatchers

class CounterViewModel : ViewModel() {

    val store = TimeTravelStore(
        delegate = CounterStore(
            coroutineContext = Dispatchers.Main.immediate,
        )
    )

    init {
        attachStore(store)
    }
}