package com.arttttt.simplemvi.notes.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.arttttt.simplemvi.notes.presentation.models.NoteListItem

@Composable
fun NoteItemContent(
    item: NoteListItem,
) {

    Text(
        text = item.message,
    )
}