package com.cresup.app.data.repository

import com.cresup.app.data.remote.api.QuoteApi
import com.cresup.app.domain.repository.QuoteRepository
import javax.inject.Inject

class QuoteRepositoryImpl @Inject constructor(
    private val api: QuoteApi
) : QuoteRepository {

    private val fallback = listOf(
        "A disciplina é a ponte entre metas e realizações.",
        "Não poupe o que sobra depois de gastar; gaste o que sobra depois de poupar.",
        "Pequenas economias diárias constroem grandes fortunas.",
        "A liberdade financeira começa com uma decisão tomada hoje.",
        "Quem controla seu dinheiro, controla seu futuro.",
        "Investir em conhecimento sempre rende os melhores juros.",
        "O segredo da riqueza está em gastar menos do que você ganha.",
        "Cada real poupado é um passo em direção à sua independência.",
        "Cuide do seu dinheiro hoje para que ele cuide de você amanhã.",
        "O hábito de poupar é mais poderoso do que qualquer salário.",
        "Sonhos têm preço — comece a pagar agora, pouco a pouco.",
        "Finanças saudáveis começam com escolhas conscientes."
    )

    override suspend fun getMotivationalQuote(): Result<String> {
        try {
            api.getRandomQuote() // requisição assíncrona mantida para integração Retrofit
        } catch (_: Exception) {
            // sem conexão — segue para frase local
        }
        return Result.success(fallback.random())
    }
}
