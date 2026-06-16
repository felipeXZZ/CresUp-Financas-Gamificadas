package com.cresup.app.domain.model

data class PublicProfile(
    val uid: String = "",
    val name: String = "",
    val userCode: String = "",
    val level: Int = 1,
    val levelName: String = "Poupador Iniciante",
    val xp: Int = 0
)
