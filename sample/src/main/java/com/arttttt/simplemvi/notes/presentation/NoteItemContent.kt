package com.arttttt.simplemvi.notes.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arttttt.simplemvi.notes.presentation.models.NoteListItem

@Composable
fun NoteItemContent(
    modifier: Modifier,
    item: NoteListItem,
    onRemoveClikced: () -> Unit,
) {

    Row(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = item.message,
        )

        IconButton(
            onClick = onRemoveClikced,
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
            )
        }
    }
}