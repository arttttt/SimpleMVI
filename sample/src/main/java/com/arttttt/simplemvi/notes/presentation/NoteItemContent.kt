package com.arttttt.simplemvi.notes.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arttttt.simplemvi.notes.presentation.models.NoteListItem

@Composable
fun NoteItemContent(
    modifier: Modifier,
    item: NoteListItem,
) {

    Box(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = item.message,
        )
    }
}