package com.cresup.app.domain.model

enum class FeedItemType(val label: String) {
    ACHIEVEMENT("Conquista"),
    LEVEL_UP("Nível"),
    CHALLENGE("Desafio"),
    STREAK("Streak")
}

data class FeedItem(
    val id: String = "",
    val type: FeedItemType = FeedItemType.ACHIEVEMENT,
    val actorName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
