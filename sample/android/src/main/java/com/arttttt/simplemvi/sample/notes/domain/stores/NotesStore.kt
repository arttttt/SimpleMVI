package com.arttttt.simplemvi.sample.notes.domain.stores

import com.arttttt.simplemvi.actor.dsl.actorDsl
import com.arttttt.simplemvi.logging.loggingActor
import com.arttttt.simplemvi.sample.notes.domain.models.Note
import com.arttttt.simplemvi.sample.notes.domain.repository.NotesRepository
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid

class NotesStore(
    coroutineContext: CoroutineContext,
    notesRepository: NotesRepository,
) : Store<NotesStore.Intent, NotesStore.State, NotesStore.SideEffect> by createStore(
    initialState = State(
        currentMessage = "",
        isInProgress = false,
        notes = emptyList(),
    ),
    initialIntents = listOf(Intent.LoadNotes),
    middlewares = emptyList(),
    actor = loggingActor(
        name = NotesStore::class.simpleName,
        delegate = actorDsl(
            coroutineContext = coroutineContext,
        ) {
            onIntent<Intent.LoadNotes> {
                launch {
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
                launch {
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
                launch {
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
        }
    )
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