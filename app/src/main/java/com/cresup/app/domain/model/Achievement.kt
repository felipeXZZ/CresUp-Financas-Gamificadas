package com.cresup.app.domain.model

enum class AchievementRarity(val label: String) {
    COMMON("Comum"),
    RARE("Raro"),
    EPIC("Épico"),
    LEGENDARY("Lendário")
}

data class Achievement(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val emoji: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val rarity: AchievementRarity = AchievementRarity.COMMON
)

val AllAchievements = listOf(
    Achievement(1L,  "Primeiro Passo",      "Registrou sua primeira transação",        "💳", rarity = AchievementRarity.COMMON),
    Achievement(2L,  "Economizador",         "Economizou pela primeira vez",             "💰", rarity = AchievementRarity.COMMON),
    Achievement(3L,  "Streak de Fogo",       "Manteve 7 dias de streak",                "🔥", rarity = AchievementRarity.RARE),
    Achievement(4L,  "Meta Alcançada",       "Completou sua primeira meta",             "🎯", rarity = AchievementRarity.RARE),
    Achievement(5L,  "Disciplinado",         "30 dias de streak consecutivo",           "🏆", rarity = AchievementRarity.EPIC),
    Achievement(6L,  "Investidor",           "Registrou primeiro investimento",          "📈", rarity = AchievementRarity.RARE),
    Achievement(7L,  "Desafiador",           "Completou primeiro desafio",              "⚡", rarity = AchievementRarity.COMMON),
    Achievement(8L,  "Elite Financeiro",     "Alcançou nível Elite Financeira",         "👑", rarity = AchievementRarity.LEGENDARY),
    Achievement(9L,  "Sem Delivery",         "Completou 7 dias sem delivery",           "🌿", rarity = AchievementRarity.RARE),
    Achievement(10L, "Milionário do XP",     "Acumulou 5.000 XP",                       "💎", rarity = AchievementRarity.EPIC),
    Achievement(11L, "Construtor",           "Criou 5 metas financeiras",               "🏗️", rarity = AchievementRarity.RARE),
    Achievement(12L, "Centurião",            "Registrou 100 transações",                "💯", rarity = AchievementRarity.EPIC),
    Achievement(13L, "Poupador de Elite",    "Economizou R\$1.000 ao total",            "🏦", rarity = AchievementRarity.EPIC),
    Achievement(14L, "Conquistador",         "Completou todos os desafios disponíveis", "🎖️", rarity = AchievementRarity.LEGENDARY),
    Achievement(15L, "Maratonista",          "100 dias de streak",                      "🏃", rarity = AchievementRarity.LEGENDARY)
)
