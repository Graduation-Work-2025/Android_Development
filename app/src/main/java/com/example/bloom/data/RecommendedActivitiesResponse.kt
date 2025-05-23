package com.example.bloom.data

import com.google.gson.annotations.SerializedName

data class RecommendedActivitiesResponse(
    @SerializedName("category") val category: String,
    @SerializedName("content") val content: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("story_id") val storyId: Int
)
