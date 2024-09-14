package com.arttttt.simplemvi.notes.presentation.models

import com.arttttt.simplemvi.ui.ListItem

data class NoteListItem(
    val id: String,
    val message: String,
) : ListItem {

    override val key: Any by this::id
}