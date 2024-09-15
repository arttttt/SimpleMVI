package com.arttttt.simplemvi.sample.notes.domain.repository

import com.arttttt.simplemvi.sample.notes.domain.models.Note

interface NotesRepository {

    suspend fun getNotes(): List<Note>
    suspend fun addNote(note: Note)
    suspend fun removeNote(id: String)

}