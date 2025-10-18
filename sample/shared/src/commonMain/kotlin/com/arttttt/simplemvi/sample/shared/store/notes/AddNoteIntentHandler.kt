package com.arttttt.simplemvi.sample.shared.store.notes

import com.arttttt.simplemvi.sample.shared.model.Note
import com.arttttt.simplemvi.sample.shared.repository.NotesRepository
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
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