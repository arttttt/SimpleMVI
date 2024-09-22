package com.arttttt.simplemvi.sample.notes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.arttttt.simplemvi.sample.notes.data.database.dao.NotesDao
import com.arttttt.simplemvi.sample.notes.data.database.models.NoteDbModel

@Database(
    entities = [
        NoteDbModel::class
    ],
    version = 1,
    exportSchema = false,
)
abstract class NotesDatabase : RoomDatabase() {

    companion object {

        fun create(
            context: Context
        ): NotesDatabase {
            return Room
                .databaseBuilder(
                    context = context,
                    klass = NotesDatabase::class.java,
                    name = "notes"
                )
                .build()

        }
    }

    abstract fun getNotesDao(): NotesDao
}