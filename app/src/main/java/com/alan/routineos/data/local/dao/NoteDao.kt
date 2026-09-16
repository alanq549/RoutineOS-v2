package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes 
        WHERE instanceId = :instanceId 
        OR (instanceId IS NULL AND dateSnapshot = :date AND titleSnapshot = :title)
    """)
    fun getNotesByQuery(instanceId: String?, date: Long, title: String): Flow<List<NoteEntity>>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE instanceId = :instanceId")
    suspend fun deleteNoteByInstanceId(instanceId: String)

    @Query("SELECT * FROM notes WHERE dateSnapshot = :date")
    fun getNotesForDate(date: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE instanceId = :instanceId LIMIT 1")
    suspend fun getNoteByInstanceId(instanceId: String): NoteEntity?
}
