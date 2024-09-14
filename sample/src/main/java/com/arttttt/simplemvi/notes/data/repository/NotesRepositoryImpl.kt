package com.arttttt.simplemvi.notes.data.repository

import com.arttttt.simplemvi.notes.data.database.dao.NotesDao
import com.arttttt.simplemvi.notes.data.database.models.NoteDbModel
import com.arttttt.simplemvi.notes.domain.models.Note
import com.arttttt.simplemvi.notes.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow

class NotesRepositoryImpl(
    private val notesDao: NotesDao,
) : NotesRepository {

    override suspend fun getNotes(): List<Note> {
        return notesDao
            .getAllNotes()
            .map { note ->
                note.toDomain()
            }
    }

    override suspend fun addNote(note: Note) {
        notesDao.addNote(note.toDbModel())
    }

    override suspend fun removeNote(id: String) {
        notesDao.deleteNoteById(id)
    }

    private fun Note.toDbModel(): NoteDbModel {
        return NoteDbModel(
            id = id,
            message = message,
        )
    }

    private fun NoteDbModel.toDomain(): Note {
        return Note(
            id = id,
            message = message,
        )
    }
}