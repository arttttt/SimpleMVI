package com.arttttt.simplemvi.sample.notes.domain.stores

import com.arttttt.simplemvi.sample.notes.domain.repository.NotesRepository
import kotlinx.coroutines.launch

fun loadNotesIntentHandler(
    notesRepository: NotesRepository,
) = notesStoreIntentHandler<NotesStore.Intent.LoadNotes> { intent ->
    scope.launch {
        reduce {
            copy(
                isInProgress = true,
            )
        }

        val notes = notesRepository.getNotes()

        reduce {
            copy(
                notes = notes,
            )
        }
    }
        .invokeOnCompletion {
            reduce {
                copy(
                    isInProgress = false,
                )
            }
        }
}