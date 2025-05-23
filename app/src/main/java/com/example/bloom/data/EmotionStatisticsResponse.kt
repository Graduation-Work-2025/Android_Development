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
) {
    fun toStatisticsMap(): Map<String, Float> {
        val total = happy + sad + fear + disgust + surprised + angry
        if (total == 0) return emptyMap()

        return mapOf(
            "기쁨" to (happy.toFloat() / total * 100),
            "슬픔" to (sad.toFloat() / total * 100),
            "공포" to (fear.toFloat() / total * 100),
            "혐오" to (disgust.toFloat() / total * 100),
            "놀람" to (surprised.toFloat() / total * 100),
            "분노" to (angry.toFloat() / total * 100)
        )
    }
}
