package com.arttttt.simplemvi.sample.notes.presentation.models

import com.arttttt.simplemvi.sample.ui.ListItem

data class NoteListItem(
    val id: String,
    val message: String,
) : ListItem {

    override val key: Any by this::id
}