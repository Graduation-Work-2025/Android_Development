package com.example.bloom.data

import com.example.bloom.R
//import com.google.android.gms.maps.model.BitmapDescriptorFactory

// 스토리 작성 요청
// 스토리 작성 요청
data class StoryPostRequest(
    val content: String,
    val longitude: Double,
    val latitude: Double,
    val sharing_type: String,
    val emotion_type: String,
    val image_url: String? = null,
    val marker_image: Int? = null // ✅ 추가된 필드
)



// 스토리 작성 내용
data class StoryContent(
    val content: String,
    val longitude: Double,
    val latitude: Double,
    val sharing_type: String,
    val emotion_type: String,
    val image_url: String? = null
)

// 스토리 작성 응답
data class StoryPostResponse(
    val content: String,
    val longitude: Double,
    val latitude: Double,
    val likes: Int,
    val user_id: Int,
    val emotion_type: String,
    val bloom_id: Int,
    val sharing_type: String,
    val image_url: String?,
    val created_at: String,
    val remind_story: Int
)

// 스토리 데이터
data class StoryData(
    val id: Int,
    val content: String,
    val longitude: Double,
    val latitude: Double,
    val likes: Int,
    val user_id: Int,
    val emotion_type: String,
    val bloom_id: Int,
    val sharing_type: String,
    val image_url: String,
    val created_at: String
)

// 위치 기반 스토리 목록 응답
data class StoryListResponse(
    val stories: List<StoryData>
)

// FeedFlower 데이터 클래스
data class FeedFlower(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val emotion: String
)
// 추천 활동 응답 데이터
data class RecommendationResponse(
    val category: String,
    val content: String,
    val reason: String,
    val story_id: Int
)





