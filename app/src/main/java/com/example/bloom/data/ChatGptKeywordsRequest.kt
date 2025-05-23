package com.example.bloom.data

import com.google.gson.annotations.SerializedName

data class ChatGptKeywordsRequest(
    @SerializedName("stories")
    val stories: List<Story>
) {
    data class Story(
        @SerializedName("storyId")
        val storyId: Int,

        @SerializedName("createdAt")
        val createdAt: String,

        @SerializedName("emotion")
        val emotion: String,

        @SerializedName("content")
        val content: String
    )
}
