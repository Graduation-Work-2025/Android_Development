package com.example.bloom.data

import com.google.gson.annotations.SerializedName

data class EmotionStatisticsResponse(
    @SerializedName("happy")
    val happy: Int,

    @SerializedName("sad")
    val sad: Int,

    @SerializedName("fear")
    val fear: Int,

    @SerializedName("disgust")
    val disgust: Int,

    @SerializedName("surprised")
    val surprised: Int,

    @SerializedName("angry")
    val angry: Int
)
