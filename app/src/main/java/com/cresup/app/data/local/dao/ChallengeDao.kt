package com.cresup.app.data.local.dao

import androidx.room.*
import com.cresup.app.data.local.entities.ChallengeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY isCompleted ASC, isActive DESC")
    fun getAllChallenges(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(challenge: ChallengeEntity): Long

    @Update
    suspend fun update(challenge: ChallengeEntity)

    @Query("SELECT COUNT(*) FROM challenges")
    suspend fun count(): Int

    @Query("UPDATE challenges SET isActive = 1, startedAt = :startedAt WHERE id = :id")
    suspend fun activate(id: Long, startedAt: Long)

    @Query("UPDATE challenges SET progressCurrent = progressCurrent + 1 WHERE id = :id")
    suspend fun incrementProgress(id: Long)

    @Query("UPDATE challenges SET isCompleted = 1, isActive = 0 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getById(id: Long): ChallengeEntity?
}
