package com.arttttt.simplemvi.notes.domain.repository

import com.arttttt.simplemvi.notes.domain.models.Note

interface NotesRepository {

    suspend fun getNotes(): List<Note>
    suspend fun addNote(note: Note)
    suspend fun removeNote(id: String)

}