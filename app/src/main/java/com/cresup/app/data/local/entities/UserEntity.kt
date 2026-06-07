package com.cresup.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cresup.app.domain.model.User
import com.cresup.app.domain.model.computeLevel
import com.cresup.app.domain.model.xpToNextLevel

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long = 1L,
    val name: String = "Usuário",
    val avatarEmoji: String = "🦁",
    val level: Int = 1,
    val levelName: String = "Poupador Iniciante",
    val xp: Int = 0,
    val xpToNextLevel: Int = 500,
    val streakDays: Int = 0,
    val maxStreak: Int = 0,
    val totalSaved: Double = 0.0,
    val goalsCompleted: Int = 0,
    val lastActivityDate: Long = System.currentTimeMillis()
)

fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    avatarEmoji = avatarEmoji,
    level = level,
    levelName = levelName,
    xp = xp,
    xpToNextLevel = xpToNextLevel,
    streakDays = streakDays,
    maxStreak = maxStreak,
    totalSaved = totalSaved,
    goalsCompleted = goalsCompleted,
    lastActivityDate = lastActivityDate
)

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    avatarEmoji = avatarEmoji,
    level = level,
    levelName = levelName,
    xp = xp,
    xpToNextLevel = xpToNextLevel,
    streakDays = streakDays,
    maxStreak = maxStreak,
    totalSaved = totalSaved,
    goalsCompleted = goalsCompleted,
    lastActivityDate = lastActivityDate
)
