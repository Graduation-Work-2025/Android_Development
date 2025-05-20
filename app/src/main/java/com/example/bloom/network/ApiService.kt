package com.example.bloom.network

import com.example.bloom.data.*
import com.example.bloom.data.GptRequestBody
import com.example.bloom.data.GptResponse
import retrofit2.Response
import retrofit2.http.*

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


    // 추천 활동 가져오기 (최신 글 기반)
    @GET("reports/recommend")
    suspend fun getRecommendations(
        @Header("Authorization") token: String
    ): Response<List<RecommendationResponse>>

    // ✅ ChatGPT 요청 (테스트)
    @POST("chat-gpt/test")
    suspend fun requestChatGptTest(
        @Query("emotion") emotion: String
    ): Response<String>

}
