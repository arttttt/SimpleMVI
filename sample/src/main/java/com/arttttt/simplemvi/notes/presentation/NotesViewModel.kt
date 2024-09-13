package com.arttttt.simplemvi.notes.presentation

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.notes.domain.stores.NotesStore
import com.arttttt.simplemvi.utils.attachStore
import kotlinx.coroutines.Dispatchers

class NotesViewModel : ViewModel() {

    private val store = NotesStore(
        coroutineContext = Dispatchers.Main.immediate,
    )

    init {
        attachStore(store)
    }
}