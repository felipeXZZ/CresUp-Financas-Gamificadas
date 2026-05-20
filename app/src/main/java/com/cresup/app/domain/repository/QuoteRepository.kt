package com.cresup.app.domain.repository

interface QuoteRepository {
    suspend fun getMotivationalQuote(): Result<String>
}
