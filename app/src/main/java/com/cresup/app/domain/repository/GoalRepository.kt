package com.cresup.app.domain.repository

import com.cresup.app.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    suspend fun insert(goal: Goal)
    suspend fun update(goal: Goal)
    suspend fun delete(goal: Goal)
    suspend fun addContribution(goalId: Long, amount: Double)
}
