package com.example.wheretime.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val subcategory: Subcategory,

    val date: LocalDate,
    val hours: Int,
    val minutes: Int,
    val note: String? = null
) {
    val totalMinutes: Int
        get() = hours * 60 + minutes
}
