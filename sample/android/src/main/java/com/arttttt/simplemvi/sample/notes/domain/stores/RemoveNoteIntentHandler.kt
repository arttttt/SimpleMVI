package com.arttttt.simplemvi.sample.notes.domain.stores

import com.arttttt.simplemvi.sample.notes.domain.repository.NotesRepository
import kotlinx.coroutines.launch

fun removeNoteIntentHandler(
    notesRepository: NotesRepository,
) = notesStoreIntentHandler<NotesStore.Intent.RemoveNote> { intent ->
    scope.launch {
        notesRepository.removeNote(intent.id)

        reduce {
            copy(
                notes = state.notes.filter { it.id != intent.id },
            )
        }
    }
}