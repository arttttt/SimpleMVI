package com.arttttt.simplemvi.sample.notes.domain.stores

fun currentMessageChangedIntentHandler() = notesStoreIntentHandler<NotesStore.Intent.CurrentMessageChanged> { intent ->
    reduce {
        copy(
            currentMessage = intent.message,
        )
    }
}