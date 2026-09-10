package com.example.wheretime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalRepository {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGoal(goal: Goal)

    @Query("SELECT * FROM goal WHERE id = ${Goal.CURRENT_GOAL_ID} LIMIT 1")
    fun getCurrentGoal(): Flow<Goal?>
}