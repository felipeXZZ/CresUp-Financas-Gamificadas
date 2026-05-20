package com.cresup.app.data.local.dao

import androidx.room.*
import com.cresup.app.data.local.entities.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): GoalEntity?

    @Query("UPDATE goals SET currentAmount = currentAmount + :amount WHERE id = :id")
    suspend fun addContribution(id: Long, amount: Double)

    @Query("UPDATE goals SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)
}
