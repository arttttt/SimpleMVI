package com.arttttt.simplemvi.sample.shared.store.notes

import com.arttttt.simplemvi.actor.dsl.delegatedActor
import com.arttttt.simplemvi.annotations.DelegatedStore
import com.arttttt.simplemvi.sample.shared.model.Note
import com.arttttt.simplemvi.sample.shared.repository.NotesRepository
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName
import kotlin.coroutines.CoroutineContext

@DelegatedStore
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
    actor = delegatedActor(
        intentHandlers = listOf(
            addNoteIntentHandler(notesRepository),
            currentMessageChangedIntentHandler(),
            loadNotesIntentHandler(notesRepository),
            removeNoteIntentHandler(notesRepository),
        ),
    ),
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