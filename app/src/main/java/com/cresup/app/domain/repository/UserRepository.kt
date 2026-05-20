package com.cresup.app.domain.repository

import com.cresup.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<User>
    suspend fun updateUser(user: User)
    suspend fun addXP(amount: Int)
    suspend fun updateStreak()
    suspend fun createDefaultUser()
}
