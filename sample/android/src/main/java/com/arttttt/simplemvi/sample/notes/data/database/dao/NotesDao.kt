package com.arttttt.simplemvi.sample.notes.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.arttttt.simplemvi.sample.notes.data.database.models.NoteDbModel

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes_table")
    suspend fun getAllNotes(): List<NoteDbModel>

    @Query("DELETE FROM notes_table WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Insert
    suspend fun addNote(note: NoteDbModel)
}