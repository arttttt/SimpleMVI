package com.arttttt.simplemvi.sample.shared.store.notes

import com.arttttt.simplemvi.actor.dsl.delegatedActor
import com.arttttt.simplemvi.annotations.DelegatedStore
import com.arttttt.simplemvi.annotations.TCAFeature
import com.arttttt.simplemvi.sample.shared.model.Note
import com.arttttt.simplemvi.sample.shared.repository.NotesRepository
import com.arttttt.simplemvi.store.Store
import com.arttttt.simplemvi.store.createStore
import com.arttttt.simplemvi.store.storeName
import kotlin.coroutines.CoroutineContext

enum class MyEnum(
    val value: Int,
) {

    CASE1(0),
    CASE2(1);
}

@TCAFeature
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
    plugins = emptyList(),
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
        val s1: Array<Note> = emptyArray(),
        val s2: Map<String, Note> = emptyMap(),
        val s3: Set<Note> = emptySet(),
        val s4: LinkedHashSet<String> = LinkedHashSet(),
        val s5: LinkedHashMap<Int, Note> = LinkedHashMap(),
        val s7: LinkedHashMap<UInt, Byte> = LinkedHashMap(),
        val s8: LinkedHashMap<Long, Double> = LinkedHashMap(),
        val s9: LinkedHashMap<String, Double> = LinkedHashMap(),
        val s10: Set<String> = emptySet(),
        val s11: MutableSet<String> = mutableSetOf(),
        val s12: MyEnum = MyEnum.CASE1,
        val s13: Long = 0,
    )

    sealed interface SideEffect
}