package com.arttttt.simplemvi.notes.presentation

import androidx.lifecycle.ViewModel
import com.arttttt.simplemvi.notes.domain.repository.NotesRepository
import com.arttttt.simplemvi.notes.domain.stores.NotesStore
import com.arttttt.simplemvi.utils.attachStore
import kotlinx.coroutines.Dispatchers

class NotesViewModel(
    notesRepository: NotesRepository,
) : ViewModel() {

    private val store = NotesStore(
        coroutineContext = Dispatchers.Main.immediate,
        notesRepository = notesRepository,
    )

    init {
        attachStore(store)
    }
}