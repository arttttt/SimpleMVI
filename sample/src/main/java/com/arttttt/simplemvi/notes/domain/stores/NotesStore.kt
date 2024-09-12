package com.arttttt.simplemvi.notes.domain.stores

import android.util.Log
import com.arttttt.simplemvi.notes.domain.models.Note
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.utils.actorDsl
import com.arttttt.simplemvi.utils.createStore
import com.arttttt.simplemvi.utils.loggingActor
import kotlin.coroutines.CoroutineContext

class NotesStore(
    coroutineContext: CoroutineContext,
) : Store<NotesStore.Intent, NotesStore.State, NotesStore.SideEffect> by createStore(
    initialState = State(
        isInProgress = false,
        notes = emptyList(),
    ),
    actor = loggingActor(
        name = "NotesStore",
        logger = { message -> Log.e("NotesStore", message) },
        delegate = actorDsl(
            coroutineContext = coroutineContext,
        ) {

        }
    )
) {

    sealed interface Intent {

        data class AddNote(val message: String) : Intent
        data class RemoveNote(val id: String) : Intent
    }

    data class State(
        val isInProgress: Boolean,
        val notes: List<Note>,
    )

    sealed interface SideEffect
}