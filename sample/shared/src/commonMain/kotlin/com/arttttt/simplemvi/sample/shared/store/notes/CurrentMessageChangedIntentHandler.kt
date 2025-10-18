package com.arttttt.simplemvi.sample.shared.store.notes

fun currentMessageChangedIntentHandler() = notesStoreIntentHandler<NotesStore.Intent.CurrentMessageChanged> { intent ->
    reduce {
        copy(
            currentMessage = intent.message,
        )
    }
}