package com.cresup.app.domain.model

data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val fromName: String = "",
    val fromCode: String = "",
    val fromLevel: Int = 1,
    val fromLevelName: String = "Poupador Iniciante",
    val fromXp: Int = 0,
    val toName: String = "",
    val toCode: String = "",
    val toLevel: Int = 1,
    val toLevelName: String = "Poupador Iniciante",
    val toXp: Int = 0,
    val status: String = "pending",
    val timestamp: Long = System.currentTimeMillis()
)
