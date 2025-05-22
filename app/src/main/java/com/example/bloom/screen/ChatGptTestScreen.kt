package com.example.bloom.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloom.R
import com.example.bloom.data.ChatGptKeywordsRequest
import com.example.bloom.network.RetrofitInstance
import com.example.bloom.util.TokenProvider
import com.google.accompanist.flowlayout.FlowRow
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ✅ 색상 정의
private val BloomPrimary = Color(0xFF55996F)
private val BloomSecondary = Color(0xFF82B69B)
private val BloomTertiary = Color(0xFFCDEADF)
private val BloomBackground = Color(0xFFF8F8F8)
private val BlockBackground = Color.White

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatGptTestScreen(navController: NavController) {
    var emotionInput by remember { mutableStateOf("") }
    var responseMessage = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = emotionInput,
                onValueChange = { emotionInput = it },
                label = { Text("감정 입력") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    if (emotionInput.isNotEmpty()) {
                        isLoading.value = true
                        requestRecommendation(emotionInput, isLoading, responseMessage)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BloomPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("요청 보내기", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (responseMessage.value.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BloomBackground,
                        contentColor = BloomPrimary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🌟 활동 추천 결과 🌟",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BloomPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = responseMessage.value,
                            fontSize = 16.sp,
                            color = BloomPrimary,
                            lineHeight = 24.sp
                        )

                        IconButton(
                            onClick = {
                                isLoading.value = true
                                requestRecommendation(emotionInput, isLoading, responseMessage)
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_reload),
                                contentDescription = "새로고침",
                                tint = BloomPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // State for weekly summaries
            val weeklySummaries = remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
            val isWeeklyLoading = remember { mutableStateOf(false) }
            val weeklyError = remember { mutableStateOf("") }
            val context = LocalContext.current

            // Fetch weekly summaries on screen load
            LaunchedEffect(Unit) {
                isWeeklyLoading.value = true
                val token = TokenProvider.getToken(context) ?: ""
                val bearerToken = "Bearer $token"

                try {
                    // Fetch stories dynamically
                    val storiesResponse = RetrofitInstance.api.getMyStories(bearerToken)
                    if (storiesResponse.isSuccessful) {
                        val stories = storiesResponse.body()?.stories?.map { story ->
                            val formattedCreatedAt = if (story.created_at.contains("T")) {
                                story.created_at
                            } else {
                                "${story.created_at}T00:00:00"
                            }

                            ChatGptKeywordsRequest.Story(
                                storyId = story.id,
                                createdAt = formattedCreatedAt,
                                emotion = story.emotion_type,
                                content = story.content ?: ""
                            )
                        } ?: emptyList()

                        val requestBody = ChatGptKeywordsRequest(stories = stories)

                        val response = RetrofitInstance.api.requestChatGptKeywords(
                            token = bearerToken,
                            requestBody = Gson().toJson(requestBody)
                        )
                        if (response.isSuccessful) {
                            val summaries = response.body()?.summaries ?: emptyMap()
                            weeklySummaries.value = summaries.mapValues { it.value.keyword }
                        } else {
                            weeklyError.value = "요청 실패: ${response.code()}"
                        }
                    } else {
                        weeklyError.value = "스토리 가져오기 실패: ${storiesResponse.code()}"
                    }
                } catch (e: Exception) {
                    weeklyError.value = "오류: ${e.message}"
                } finally {
                    isWeeklyLoading.value = false
                }
            }

            // UI for weekly summaries
            Text(
                text = "🗓️ 최근 7일 요약",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BloomPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isWeeklyLoading.value) {
                CircularProgressIndicator(color = BloomPrimary)
            } else if (weeklyError.value.isNotEmpty()) {
                Text(
                    text = "오류: ${weeklyError.value}",
                    color = Color.Red,
                    fontSize = 16.sp
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BloomBackground,
                        contentColor = BloomPrimary
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        weeklySummaries.value.forEach { (day, activities) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = when (day.lowercase()) {
                                        "monday" -> "월"
                                        "tuesday" -> "화"
                                        "wednesday" -> "수"
                                        "thursday" -> "목"
                                        "friday" -> "금"
                                        "saturday" -> "토"
                                        "sunday" -> "일"
                                        else -> day
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = BloomPrimary,
                                    modifier = Modifier.weight(0.15f)
                                )

                                FlowRow(
                                    modifier = Modifier.weight(0.85f),
                                    mainAxisSpacing = 8.dp,
                                    crossAxisSpacing = 8.dp
                                ) {
                                    activities.forEach { activity ->
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    BloomTertiary,
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .padding(vertical = 6.dp, horizontal = 12.dp)
                                        ) {
                                            Text(
                                                text = activity,
                                                color = BloomPrimary,
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ 요청 함수
private fun requestRecommendation(
    emotion: String,
    isLoading: MutableState<Boolean>,
    responseMessage: MutableState<String>
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.requestChatGptTest(emotion)
            if (response.isSuccessful) {
                val result = response.body() ?: "응답 없음"
                withContext(Dispatchers.Main) {
                    responseMessage.value = result
                    isLoading.value = false
                }
            } else {
                withContext(Dispatchers.Main) {
                    responseMessage.value = "요청 실패: ${response.code()}"
                    isLoading.value = false
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                responseMessage.value = "오류: ${e.message}"
                isLoading.value = false
            }
        }
    }
}
