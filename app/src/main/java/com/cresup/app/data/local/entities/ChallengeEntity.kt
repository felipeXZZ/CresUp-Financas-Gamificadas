package com.cresup.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cresup.app.domain.model.Challenge

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    val emoji: String,
    val xpReward: Int,
    val durationDays: Int,
    val progressCurrent: Int = 0,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val startedAt: Long? = null
)

fun ChallengeEntity.toDomain() = Challenge(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    xpReward = xpReward,
    durationDays = durationDays,
    progressCurrent = progressCurrent,
    isActive = isActive,
    isCompleted = isCompleted,
    startedAt = startedAt
)

fun Challenge.toEntity() = ChallengeEntity(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    xpReward = xpReward,
    durationDays = durationDays,
    progressCurrent = progressCurrent,
    isActive = isActive,
    isCompleted = isCompleted,
    startedAt = startedAt
)
