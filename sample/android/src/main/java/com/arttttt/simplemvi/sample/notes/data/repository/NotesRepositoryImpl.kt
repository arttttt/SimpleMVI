package com.arttttt.simplemvi.sample.notes.data.repository

import com.arttttt.simplemvi.sample.notes.data.database.dao.NotesDao
import com.arttttt.simplemvi.sample.notes.data.database.models.NoteDbModel
import com.arttttt.simplemvi.sample.shared.model.Note
import com.arttttt.simplemvi.sample.shared.repository.NotesRepository

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