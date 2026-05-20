package com.cresup.app.data.repository

import com.cresup.app.data.remote.api.QuoteApi
import com.cresup.app.domain.repository.QuoteRepository
import javax.inject.Inject

class QuoteRepositoryImpl @Inject constructor(
    private val api: QuoteApi
) : QuoteRepository {

    private val fallbackQuotes = listOf(
        "Discipline is the bridge between goals and accomplishment.",
        "A penny saved is a penny earned.",
        "Do not save what is left after spending, but spend what is left after saving.",
        "Financial freedom is available to those who learn about it and work for it.",
        "The secret to wealth is simple: find a way to do more for others than anyone else does.",
        "It's not your salary that makes you rich, it's your spending habits."
    )

    override suspend fun getMotivationalQuote(): Result<String> = runCatching {
        val quotes = api.getRandomQuote()
        quotes.firstOrNull()?.quote ?: fallbackQuotes.random()
    }.recoverCatching {
        fallbackQuotes.random()
    }
}
