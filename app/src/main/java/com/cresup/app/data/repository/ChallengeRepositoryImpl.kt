package com.cresup.app.data.repository

import com.cresup.app.data.local.dao.ChallengeDao
import com.cresup.app.data.local.entities.toDomain
import com.cresup.app.data.local.entities.toEntity
import com.cresup.app.domain.model.Challenge
import com.cresup.app.domain.model.DefaultChallenges
import com.cresup.app.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChallengeRepositoryImpl @Inject constructor(
    private val dao: ChallengeDao
) : ChallengeRepository {

    override fun getAllChallenges(): Flow<List<Challenge>> =
        dao.getAllChallenges().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(challenge: Challenge) {
        dao.insert(challenge.toEntity())
    }

    override suspend fun update(challenge: Challenge) {
        dao.update(challenge.toEntity())
    }

    override suspend fun activateChallenge(id: Long) {
        dao.activate(id, System.currentTimeMillis())
    }

    override suspend fun incrementProgress(id: Long) {
        dao.incrementProgress(id)
        val ch = dao.getById(id) ?: return
        if (ch.progressCurrent + 1 >= ch.durationDays) {
            dao.markCompleted(id)
        }
    }

    override suspend fun seedDefaultChallenges() {
        if (dao.count() == 0) {
            DefaultChallenges.forEach { dao.insert(it.toEntity()) }
        }
    }
}
