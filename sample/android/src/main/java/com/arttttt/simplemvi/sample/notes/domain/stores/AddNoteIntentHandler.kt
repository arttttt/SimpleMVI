package com.arttttt.simplemvi.sample.notes.domain.stores

import com.arttttt.simplemvi.sample.notes.domain.models.Note
import com.arttttt.simplemvi.sample.notes.domain.repository.NotesRepository
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun addNoteIntentHandler(
    notesRepository: NotesRepository,
) = notesStoreIntentHandler<NotesStore.Intent.AddNote> { intent ->
    scope.launch {
        val note = Note(
            id = Uuid.random().toString(),
            message = state.currentMessage,
        )

        notesRepository.addNote(
            note = note,
        )

        reduce {
            copy(
                currentMessage = "",
                notes = state.notes + note,
            )
        }
    }
}