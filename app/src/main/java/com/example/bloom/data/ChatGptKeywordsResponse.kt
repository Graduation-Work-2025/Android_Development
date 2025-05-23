package com.example.bloom.data

import com.google.gson.annotations.SerializedName

data class ChatGptKeywordsResponse(
    @SerializedName("summaries")
    val summaries: Map<String, DaySummary>,

    @SerializedName("message")
    val message: String
)

data class DaySummary(
    @SerializedName("weekday")
    val weekday: String,

    @SerializedName("keyword")
    val keyword: List<String>
)
