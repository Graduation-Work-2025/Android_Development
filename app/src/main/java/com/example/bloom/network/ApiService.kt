package com.example.bloom.network

import com.example.bloom.data.LoginRequestDto
import com.example.bloom.data.LoginResponseDto
import com.example.bloom.data.PresignedUrlRequest
import com.example.bloom.data.PresignedUrlResponse
import com.example.bloom.data.SignUpRequestDto
import com.example.bloom.data.StoryData
import com.example.bloom.data.StoryListResponse
import com.example.bloom.data.StoryPostRequest
import com.example.bloom.data.StoryPostResponse
import com.example.bloom.data.UserData
import com.example.bloom.data.ChatGptKeywordsResponse
import com.example.bloom.data.ChatGptKeywordsRequest
import com.example.bloom.data.RecommendedActivitiesResponse
import com.example.bloom.data.EmotionStatisticsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    // ✅ 회원가입
    @POST("users/signup")
    suspend fun signUp(
        @Body signUpInfo: SignUpRequestDto
    ): Response<Unit>

    // ✅ 로그인
    @POST("users/login")
    suspend fun login(
        @Body loginInfo: LoginRequestDto
    ): Response<LoginResponseDto>

    // ✅ Presigned URL 요청
    @POST("s3/upload/url")
    suspend fun getPresignedUrl(
        @Header("Authorization") token: String,
        @Body requestBody: PresignedUrlRequest
    ): Response<PresignedUrlResponse>

    // ✅ 스토리 등록
    @POST("stories")
    suspend fun postStory(
        @Header("Authorization") token: String,
        @Body requestBody: StoryPostRequest
    ): Response<StoryPostResponse>

    // ✅ 위치 기반 주변 스토리 조회
    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<StoryListResponse>

    // ✅ 내 스토리 목록 조회
    @GET("stories/my")
    suspend fun getMyStories(
        @Header("Authorization") token: String
    ): Response<StoryListResponse>

    // ✅ 특정 스토리 가져오기
    @GET("stories/{id}")
    suspend fun getStoryById(
        @Header("Authorization") token: String,
        @Path("id") storyId: Int
    ): Response<StoryData>


    // ✅ 사용자 정보 조회 (새로운 방식)
    @GET("users/my")
    suspend fun getMyInfo(
        @Header("Authorization") token: String
    ): Response<UserData>


    // ✅ ChatGPT 요청 (테스트)
    @POST("chat-gpt/test")
    suspend fun requestChatGptTest(
        @Query("emotion") emotion: String
    ): Response<String>

    // ✅ ChatGPT 추천 요청
    // {
    //  "category": "문구",
    //  "content": "힘든 날도 분명 끝이 있을 거예요. 내일은 더 나은 날이 올 거예요.",
    //  "reason": "오늘의 힘든 날을 견디며 내일에 대한 희망을 심어주는 위로"
    //}
    @POST("chat-gpt/recommend")
    suspend fun requestChatGptRecommend(
        @Header("Authorization") token: String,
    ): Response<String>

    // ✅ ChatGPT 키워드 요청
    // {
    //  "summaries": {
    //    "monday": {
    //      "weekday": "25.05.22",
    //      "keyword": [
    //        "힘든",
    //        "하루"
    //      ]
    //    },
    //    "tuesday": {
    //      "weekday": "25.05.23",
    //      "keyword": []
    //    },
    //    "wednesday": {
    //      "weekday": "25.05.24",
    //      "keyword": []
    //    },
    //    "thursday": {
    //      "weekday": "25.05.25",
    //      "keyword": [
    //        "기쁨"
    //      ]
    //    },
    //    "friday": {
    //      "weekday": "25.05.26",
    //      "keyword": []
    //    },
    //    "saturday": {
    //      "weekday": "25.05.27",
    //      "keyword": []
    //    },
    //    "sunday": {
    //      "weekday": "25.05.28",
    //      "keyword": []
    //    }
    //  }
    //}
    @POST("chat-gpt/keywords")
    @Headers("Content-Type: application/json")
    suspend fun requestChatGptKeywords(
        @Header("Authorization") token: String,
        @Body requestBody: String
    ): Response<ChatGptKeywordsResponse>


    @GET("reports/emotions")
    suspend fun getReportsEmotions(
        @Header("Authorization") token: String,
    ): Response<StoryListResponse>

    @GET("reports/recommend")
    suspend fun getRecommendedActivities(
        @Header("Authorization") token: String,
    ): Response<RecommendedActivitiesResponse>

    @GET("reports/recommend/renewal")
    suspend fun refreshRecommendedActivities(
        @Header("Authorization") token: String,
    ): Response<RecommendedActivitiesResponse>

    // ✅ 지난 일주일 키워드 요약 불러오기
    @GET("reports/keywords")
    suspend fun getWeeklyKeywords(
        @Header("Authorization") token: String
    ): Response<ChatGptKeywordsResponse>

    // ✅ 감정 통계 불러오기
    @GET("reports/emotions")
    suspend fun getEmotionStatistics(
        @Header("Authorization") token: String
    ): Response<EmotionStatisticsResponse>

}
