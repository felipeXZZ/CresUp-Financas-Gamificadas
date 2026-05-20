package com.cresup.app.data.local.dao

import androidx.room.*
import com.cresup.app.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("UPDATE users SET xp = xp + :amount WHERE id = 1")
    suspend fun addXP(amount: Int)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}
