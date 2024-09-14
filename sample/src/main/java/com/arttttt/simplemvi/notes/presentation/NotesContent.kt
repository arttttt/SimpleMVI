package com.arttttt.simplemvi.notes.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arttttt.simplemvi.notes.data.database.NotesDatabase
import com.arttttt.simplemvi.notes.data.repository.NotesRepositoryImpl
import com.arttttt.simplemvi.notes.presentation.models.NoteListItem

@Composable
fun NotesContent() {

    val context = LocalContext.current.applicationContext

    val viewModel: NotesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            val dataBase = NotesDatabase.create(
                context = context,
            )

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotesViewModel(
                    notesRepository = NotesRepositoryImpl(
                        notesDao = dataBase.getNotesDao(),
                    ),
                ) as T
            }
        }
    )

    val uiState by viewModel.uiStates.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                vertical = 16.dp,
            ),
    ) {
        TopAppBar(
            title = {
                Text("Notes")
            },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(
                items = uiState.items,
                key = { item -> item.key },
                contentType = { item -> item::class }
            ) { item ->
                when (item) {
                    is NoteListItem -> NoteItemContent(
                        modifier = Modifier.fillParentMaxWidth(),
                        item = item,
                    )
                }
            }
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                ),
            value = uiState.currentMessage,
            onValueChange = viewModel::updateCurrentNote,
            label = {
                Text(text = "Add note")
            },
            trailingIcon = {
                if (uiState.currentMessage.isNotEmpty()) {
                    IconButton(
                        onClick = viewModel::addNote,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                        )
                    }
                }
            },
        )
    }
}