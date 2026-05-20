package com.cresup.app.domain.repository

import com.cresup.app.domain.model.Challenge
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    fun getAllChallenges(): Flow<List<Challenge>>
    suspend fun insert(challenge: Challenge)
    suspend fun update(challenge: Challenge)
    suspend fun activateChallenge(id: Long)
    suspend fun incrementProgress(id: Long)
    suspend fun seedDefaultChallenges()
}
