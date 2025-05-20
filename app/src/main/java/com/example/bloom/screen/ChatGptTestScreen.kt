package com.example.bloom.screen

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.TextFieldDefaults
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

// ✅ 색상 정의
private val BloomPrimary = Color(0xFF55996F)
private val BloomSecondary = Color(0xFF82B69B)
private val BloomTertiary = Color(0xFFCDEADF)
private val BloomBackground = Color(0xFFF8F8F8)
private val BlockBackground = Color.White

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatGptTestScreen(navController: NavController) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var emotionInput by remember { mutableStateOf("") }
            var responseMessage = remember { mutableStateOf("") }
            val isLoading = remember { mutableStateOf(false) }

            // 감정 입력 필드
            OutlinedTextField(
                value = emotionInput,
                onValueChange = { emotionInput = it },
                label = { Text("감정 입력") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // 요청 버튼
            Button(
                onClick = {
                    if (emotionInput.isNotEmpty()) {
                        isLoading.value = true
                        requestRecommendation(emotionInput, isLoading, responseMessage)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
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

            // ✅ 활동 추천 결과 카드
            if (responseMessage.value.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Text("🌟 활동 추천 결과 🌟", fontWeight = FontWeight.Bold, color = BloomPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(responseMessage.value, color = BloomPrimary, lineHeight = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ 7일 요약 표
            Text("🗓️ 최근 7일 요약", fontWeight = FontWeight.Bold, color = BloomPrimary)

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ 7일 요약 데이터 (더미 데이터)
            val weeklySummaries = mapOf(
                "월" to listOf("마라탕", "친구", "행복"),
                "화" to listOf("학교", "공부", "피곤"),
                "수" to listOf("운동", "커피", "상쾌함"),
                "목" to listOf("내용 없음"),
                "금" to listOf("회의", "피곤", "스트레스"),
                "토" to listOf("독서", "영화", "여유"),
                "일" to listOf("휴식", "낮잠", "재충전")
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BloomBackground,
                    contentColor = BloomPrimary
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    weeklySummaries.forEach { (day, activities) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(day, fontWeight = FontWeight.Bold, color = BloomPrimary, modifier = Modifier.weight(0.15f))

                            FlowRow(
                                modifier = Modifier.weight(0.85f),
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp
                            ) {
                                activities.forEach { activity ->
                                    Box(
                                        modifier = Modifier
                                            .background(BloomTertiary, RoundedCornerShape(16.dp))
                                            .padding(6.dp)
                                    ) {
                                        Text(activity, color = BloomPrimary, fontSize = 14.sp, textAlign = TextAlign.Center)
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
