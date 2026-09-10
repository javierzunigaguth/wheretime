package com.example.wheretime.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EntryRepository {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: Entry): Long

    @Delete
    suspend fun delete(entry: Entry)

    @Query(
        """
        SELECT * FROM entries
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, id DESC
        """
    )
    fun getEntriesBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<Entry>>

    @Query("SELECT * FROM entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<Entry>>
}