package com.example.bloom.screen

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.flowlayout.FlowRow
import com.example.bloom.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.painterResource
import com.example.bloom.R
import androidx.compose.ui.platform.LocalContext
import com.example.bloom.util.TokenProvider

// ✅ 색상 정의
private val BloomPrimary = Color(0xFF55996F)
private val BloomSecondary = Color(0xFF82B69B)
private val BloomTertiary = Color(0xFFCDEADF)
private val BloomBackground = Color(0xFFF8F8F8)
private val BlockBackground = Color.White

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatGptScreen(navController: NavController) {
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
                        refreshRecommendedActivities(emotionInput, isLoading, responseMessage)
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
                                refreshRecommendedActivities(emotionInput, isLoading, responseMessage)
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

            // Fetch weekly summaries on screen load
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                isWeeklyLoading.value = true
                val token = TokenProvider.getToken(context) ?: ""
                val bearerToken = "Bearer $token"

                try {
                    val response = RetrofitInstance.api.getWeeklyKeywords(
                        token = bearerToken
                    )
                    if (response.isSuccessful) {
                        val summaries = response.body()?.summaries ?: emptyMap()
                        weeklySummaries.value = summaries.mapValues { it.value.keyword }
                    } else if (response.code() == 400) {
                        weeklyError.value = response.body()?.message ?: "요청 실패: (400)"
                    } else {
                        weeklyError.value = "요청 실패: ${response.code()}"
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
                                    text = day,
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
                                                .background(BloomTertiary, RoundedCornerShape(16.dp))
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
private fun refreshRecommendedActivities(
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
