package com.cresup.app.domain.model

data class Challenge(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val emoji: String,
    val xpReward: Int,
    val durationDays: Int,
    val progressCurrent: Int = 0,
    val isActive: Boolean = false,
    val isCompleted: Boolean = false,
    val startedAt: Long? = null
) {
    val progress: Float get() = (progressCurrent.toFloat() / durationDays).coerceIn(0f, 1f)
}

val DefaultChallenges = listOf(
    Challenge(
        id = 1L,
        title = "7 Dias Sem Delivery",
        description = "Fique 7 dias seguidos sem pedir delivery",
        emoji = "🚫🛵",
        xpReward = 200,
        durationDays = 7
    ),
    Challenge(
        id = 2L,
        title = "Economize R\$100",
        description = "Economize R\$100 em uma semana",
        emoji = "💰",
        xpReward = 150,
        durationDays = 7
    ),
    Challenge(
        id = 3L,
        title = "15 Dias Registrando",
        description = "Registre seus gastos por 15 dias consecutivos",
        emoji = "📝",
        xpReward = 300,
        durationDays = 15
    ),
    Challenge(
        id = 4L,
        title = "Meta Semanal",
        description = "Conclua uma meta financeira nesta semana",
        emoji = "🎯",
        xpReward = 250,
        durationDays = 7
    ),
    Challenge(
        id = 5L,
        title = "Semana de Economia",
        description = "Gaste menos que seu orçamento diário por 7 dias",
        emoji = "📊",
        xpReward = 180,
        durationDays = 7
    )
)
