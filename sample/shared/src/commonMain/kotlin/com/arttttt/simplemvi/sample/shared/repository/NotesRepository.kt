package com.arttttt.simplemvi.sample.shared.repository

import com.arttttt.simplemvi.sample.shared.model.Note

interface NotesRepository {

    suspend fun getNotes(): List<Note>
    suspend fun addNote(note: Note)
    suspend fun removeNote(id: String)

}