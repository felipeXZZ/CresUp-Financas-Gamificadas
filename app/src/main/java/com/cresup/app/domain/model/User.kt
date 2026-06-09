package com.cresup.app.domain.model

data class User(
    val id: Long = 1L,
    val name: String = "",
    val avatarEmoji: String = "🦁",
    val level: Int = 1,
    val levelName: String = "Poupador Iniciante",
    val xp: Int = 0,
    val xpToNextLevel: Int = 500,
    val streakDays: Int = 0,
    val maxStreak: Int = 0,
    val totalSaved: Double = 0.0,
    val goalsCompleted: Int = 0,
    val coins: Int = 0,
    val lastActivityDate: Long = System.currentTimeMillis()
)

fun User.levelProgress(): Float = (xp.toFloat() / xpToNextLevel).coerceIn(0f, 1f)

fun User.nextLevel(): String = when (level) {
    1 -> "Construtor de Riqueza"
    2 -> "Especialista Financeiro"
    3 -> "Elite Financeira"
    else -> "Elite Financeira"
}

fun computeLevel(xp: Int): Pair<Int, String> = when {
    xp < 500  -> 1 to "Poupador Iniciante"
    xp < 1500 -> 2 to "Construtor de Riqueza"
    xp < 3500 -> 3 to "Especialista Financeiro"
    else      -> 4 to "Elite Financeira"
}

fun xpToNextLevel(level: Int): Int = when (level) {
    1 -> 500
    2 -> 1500
    3 -> 3500
    else -> Int.MAX_VALUE
}
