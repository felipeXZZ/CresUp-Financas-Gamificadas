package com.cresup.app.data.repository

import com.cresup.app.data.local.dao.GoalDao
import com.cresup.app.data.local.entities.toDomain
import com.cresup.app.data.local.entities.toEntity
import com.cresup.app.domain.model.Goal
import com.cresup.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> =
        dao.getAllGoals().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(goal: Goal) {
        dao.insert(goal.toEntity())
    }

    override suspend fun update(goal: Goal) {
        dao.update(goal.toEntity())
    }

    override suspend fun delete(goal: Goal) {
        dao.delete(goal.toEntity())
    }

    override suspend fun addContribution(goalId: Long, amount: Double) {
        dao.addContribution(goalId, amount)
        val goal = dao.getById(goalId) ?: return
        if (goal.currentAmount + amount >= goal.targetAmount) {
            dao.markCompleted(goalId)
        }
    }
}
