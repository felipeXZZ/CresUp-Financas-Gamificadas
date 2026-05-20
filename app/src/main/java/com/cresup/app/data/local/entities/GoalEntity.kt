package com.cresup.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cresup.app.domain.model.Goal

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val emoji: String = "🎯",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)

fun GoalEntity.toDomain() = Goal(
    id = id,
    title = title,
    emoji = emoji,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    deadline = deadline,
    createdAt = createdAt,
    isCompleted = isCompleted
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    title = title,
    emoji = emoji,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    deadline = deadline,
    createdAt = createdAt,
    isCompleted = isCompleted
)
