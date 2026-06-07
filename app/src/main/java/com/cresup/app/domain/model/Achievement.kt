package com.cresup.app.domain.model

data class Achievement(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

val AllAchievements = listOf(
    Achievement(1L, "Primeiro Passo", "Registrou sua primeira transação", "👣"),
    Achievement(2L, "Economizador", "Economizou pela primeira vez", "💰"),
    Achievement(3L, "Streak de Fogo", "Manteve 7 dias de streak", "🔥"),
    Achievement(4L, "Meta Alcançada", "Completou sua primeira meta", "🎯"),
    Achievement(5L, "Disciplinado", "30 dias de streak", "🏆"),
    Achievement(6L, "Investidor", "Registrou primeiro investimento", "📈"),
    Achievement(7L, "Desafiador", "Completou primeiro desafio", "⚡"),
    Achievement(8L, "Elite Financeiro", "Alcançou nível Elite Financeira", "👑"),
    Achievement(9L, "Sem Delivery", "Completou 7 dias sem delivery", "🌿"),
    Achievement(10L, "Milionário do XP", "Acumulou 5000 XP", "💎")
)
