package com.example.wheretime.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal")
data class Goal(
    @PrimaryKey
    val id: Int = CURRENT_GOAL_ID,

    val subcategory: Subcategory,
    val targetHours: Int,
    val targetMinutes: Int
) {
    val totalTargetMinutes: Int
        get() = targetHours * 60 + targetMinutes

    companion object {
        const val CURRENT_GOAL_ID = 0
    }
}
