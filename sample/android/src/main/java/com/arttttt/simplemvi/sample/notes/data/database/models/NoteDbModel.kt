package com.arttttt.simplemvi.sample.notes.data.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "notes_table",
    primaryKeys = [
        "id"
    ]
)
data class NoteDbModel(
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val message: String,
)