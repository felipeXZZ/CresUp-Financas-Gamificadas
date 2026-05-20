package com.cresup.app.domain.model

data class Goal(
    val id: Long = 0L,
    val title: String,
    val emoji: String = "🎯",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    val progress: Float get() = (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
    val remaining: Double get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
    val progressPercent: Int get() = (progress * 100).toInt()
}

val GoalPresets = listOf(
    "✈️" to "Viagem",
    "🎮" to "Setup Gamer",
    "📱" to "iPhone",
    "🚗" to "Carro",
    "🛡️" to "Reserva Financeira",
    "🌍" to "Intercâmbio",
    "🏠" to "Apartamento",
    "💎" to "Investimento"
)
