package com.arttttt.simplemvi.sample.shared.store.notes

import com.arttttt.simplemvi.sample.shared.repository.NotesRepository
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