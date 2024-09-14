package com.arttttt.simplemvi.notes.domain.stores

import android.util.Log
import com.arttttt.simplemvi.notes.domain.models.Note
import com.arttttt.simplemvi.notes.domain.repository.NotesRepository
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.utils.actorDsl
import com.arttttt.simplemvi.utils.createStore
import com.arttttt.simplemvi.utils.loggingActor
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class NotesStore(
    coroutineContext: CoroutineContext,
    notesRepository: NotesRepository,
) : Store<NotesStore.Intent, NotesStore.State, NotesStore.SideEffect> by createStore(
    initialState = State(
        isInProgress = false,
        notes = emptyList(),
    ),
    initialIntents = listOf(Intent.LoadNotes),
    actor = loggingActor(
        name = "NotesStore",
        logger = { message -> Log.e("NotesStore", message) },
        delegate = actorDsl(
            coroutineContext = coroutineContext,
        ) {
            onIntent<Intent.LoadNotes> {
                launch {
                    reduce { state ->
                        state.copy(
                            isInProgress = true,
                        )
                    }

                    val notes = notesRepository.getNotes()

                    reduce { state ->
                        state.copy(
                            notes = notes,
                        )
                    }
                }
                    .invokeOnCompletion {
                        reduce { state ->
                            state.copy(
                                isInProgress = false,
                            )
                        }
                    }
            }

            onIntent<Intent.AddNote> { intent ->
                launch {
                    val note = Note(
                        id = Uuid.random().toString(),
                        message = intent.message,
                    )

                    notesRepository.addNote(
                        note = note,
                    )

                    reduce { state ->
                        state.copy(
                            notes = state.notes + note,
                        )
                    }
                }
            }

            onIntent<Intent.RemoveNote> { intent ->
                launch {
                    notesRepository.removeNote(intent.id)

                    reduce { state ->
                        state.copy(
                            notes = state.notes.filter { it.id != intent.id },
                        )
                    }
                }
            }
        }
    )
) {

    sealed interface Intent {

        data object LoadNotes : Intent

        data class AddNote(val message: String) : Intent
        data class RemoveNote(val id: String) : Intent
    }

    data class State(
        val isInProgress: Boolean,
        val notes: List<Note>,
    )

    sealed interface SideEffect
}