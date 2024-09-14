package com.arttttt.simplemvi.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arttttt.simplemvi.notes.domain.repository.NotesRepository
import com.arttttt.simplemvi.notes.domain.stores.NotesStore
import com.arttttt.simplemvi.notes.presentation.models.NoteListItem
import com.arttttt.simplemvi.ui.ListItem
import com.arttttt.simplemvi.utils.attachStore
import com.arttttt.simplemvi.utils.plus
import com.arttttt.simplemvi.utils.state
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NotesViewModel(
    notesRepository: NotesRepository,
) : ViewModel() {

    data class UiState(
        val currentMessage: String,
        val items: List<ListItem>,
    )

    private val store = NotesStore(
        coroutineContext = Dispatchers.Main.immediate,
        notesRepository = notesRepository,
    )

    val uiStates: StateFlow<UiState>

    init {
        attachStore(store)

        uiStates = store
            .states
            .map { state -> state.toUiModel() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = store.state.toUiModel(),
            )
    }

    fun addNote() {
        store + NotesStore.Intent.AddNote
    }

    fun updateCurrentNote(message: String) {
        store + NotesStore.Intent.CurrentMessageChanged(message)
    }

    fun removeNote(id: String) {
        store + NotesStore.Intent.RemoveNote(id)
    }

    private fun NotesStore.State.toUiModel(): UiState {
        return UiState(
            currentMessage = currentMessage,
            items = notes.map { note ->
                NoteListItem(
                    id = note.id,
                    message = note.message,
                )
            },
        )
    }
}