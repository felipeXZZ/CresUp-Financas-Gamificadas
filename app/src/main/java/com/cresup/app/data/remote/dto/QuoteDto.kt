package com.cresup.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuoteDto(
    @SerializedName("q") val quote: String,
    @SerializedName("a") val author: String,
    @SerializedName("h") val html: String = ""
)
