package com.cresup.app.data.remote.api

import com.cresup.app.data.remote.dto.QuoteDto
import retrofit2.http.GET

interface QuoteApi {
    @GET("random")
    suspend fun getRandomQuote(): List<QuoteDto>
}
