package com.arttttt.simplemvi.sample.notes.domain.stores

import com.arttttt.simplemvi.actor.delegated.actorDsl
import com.arttttt.simplemvi.sample.notes.domain.models.Note
import com.arttttt.simplemvi.sample.notes.domain.repository.NotesRepository
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class NotesStore(
    coroutineContext: CoroutineContext,
    notesRepository: NotesRepository,
) : Store<NotesStore.Intent, NotesStore.State, NotesStore.SideEffect> by createStore(
    name = storeName<NotesStore>(),
    coroutineContext = coroutineContext,
    initialState = State(
        currentMessage = "",
        isInProgress = false,
        notes = emptyList(),
    ),
    initialIntents = listOf(Intent.LoadNotes),
    middlewares = emptyList(),
    actor = actorDsl {
        onIntent<Intent.LoadNotes> {
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

        onIntent<Intent.AddNote> {
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

        onIntent<Intent.RemoveNote> { intent ->
            scope.launch {
                notesRepository.removeNote(intent.id)

                reduce {
                    copy(
                        notes = state.notes.filter { it.id != intent.id },
                    )
                }
            }
        }

        onIntent<Intent.CurrentMessageChanged> { intent ->
            reduce {
                copy(
                    currentMessage = intent.message,
                )
            }
        }
    },
) {

    sealed interface Intent {

        data object LoadNotes : Intent

        data object AddNote : Intent
        data class RemoveNote(val id: String) : Intent

        data class CurrentMessageChanged(val message: String) : Intent
    }

    data class State(
        val currentMessage: String,
        val isInProgress: Boolean,
        val notes: List<Note>,
    )

    sealed interface SideEffect
}