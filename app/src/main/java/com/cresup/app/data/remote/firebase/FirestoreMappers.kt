package com.cresup.app.data.remote.firebase

import com.cresup.app.domain.model.*
import com.google.firebase.firestore.DocumentSnapshot

// ─── User ───────────────────────────────────────────────────────────────────

fun DocumentSnapshot.toUser(): User = User(
    name = getString("name") ?: "",
    avatarEmoji = getString("avatarEmoji") ?: "🦁",
    level = getLong("level")?.toInt() ?: 1,
    levelName = getString("levelName") ?: "Poupador Iniciante",
    xp = getLong("xp")?.toInt() ?: 0,
    xpToNextLevel = getLong("xpToNextLevel")?.toInt() ?: 500,
    streakDays = getLong("streakDays")?.toInt() ?: 0,
    maxStreak = getLong("maxStreak")?.toInt() ?: 0,
    totalSaved = getDouble("totalSaved") ?: 0.0,
    goalsCompleted = getLong("goalsCompleted")?.toInt() ?: 0,
    coins = getLong("coins")?.toInt() ?: 0,
    lastActivityDate = getLong("lastActivityDate") ?: System.currentTimeMillis()
)

fun User.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "avatarEmoji" to avatarEmoji,
    "level" to level,
    "levelName" to levelName,
    "xp" to xp,
    "xpToNextLevel" to xpToNextLevel,
    "streakDays" to streakDays,
    "maxStreak" to maxStreak,
    "totalSaved" to totalSaved,
    "goalsCompleted" to goalsCompleted,
    "coins" to coins,
    "lastActivityDate" to lastActivityDate
)

// ─── Transaction ─────────────────────────────────────────────────────────────

fun DocumentSnapshot.toTransaction(): Transaction? {
    val type = runCatching {
        TransactionType.valueOf(getString("type") ?: "EXPENSE")
    }.getOrNull() ?: return null
    val category = runCatching {
        TransactionCategory.valueOf(getString("category") ?: "OTHER")
    }.getOrNull() ?: TransactionCategory.OTHER
    return Transaction(
        id = getLong("id") ?: 0L,
        title = getString("title") ?: "",
        amount = getDouble("amount") ?: 0.0,
        type = type,
        category = category,
        date = getLong("date") ?: System.currentTimeMillis(),
        note = getString("note") ?: ""
    )
}

fun Transaction.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "amount" to amount,
    "type" to type.name,
    "category" to category.name,
    "date" to date,
    "note" to note
)

// ─── Goal ────────────────────────────────────────────────────────────────────

fun DocumentSnapshot.toGoal(): Goal = Goal(
    id = getLong("id") ?: 0L,
    title = getString("title") ?: "",
    emoji = getString("emoji") ?: "🎯",
    targetAmount = getDouble("targetAmount") ?: 0.0,
    currentAmount = getDouble("currentAmount") ?: 0.0,
    deadline = getLong("deadline"),
    createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
    isCompleted = getBoolean("isCompleted") ?: false
)

fun Goal.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "emoji" to emoji,
    "targetAmount" to targetAmount,
    "currentAmount" to currentAmount,
    "deadline" to deadline,
    "createdAt" to createdAt,
    "isCompleted" to isCompleted
)

// ─── Challenge ───────────────────────────────────────────────────────────────

fun DocumentSnapshot.toChallenge(): Challenge = Challenge(
    id = getLong("id") ?: 0L,
    title = getString("title") ?: "",
    description = getString("description") ?: "",
    emoji = getString("emoji") ?: "🏆",
    xpReward = getLong("xpReward")?.toInt() ?: 0,
    durationDays = getLong("durationDays")?.toInt() ?: 7,
    progressCurrent = getLong("progressCurrent")?.toInt() ?: 0,
    isActive = getBoolean("isActive") ?: false,
    isCompleted = getBoolean("isCompleted") ?: false,
    startedAt = getLong("startedAt")
)

fun Challenge.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "description" to description,
    "emoji" to emoji,
    "xpReward" to xpReward,
    "durationDays" to durationDays,
    "progressCurrent" to progressCurrent,
    "isActive" to isActive,
    "isCompleted" to isCompleted,
    "startedAt" to startedAt
)
