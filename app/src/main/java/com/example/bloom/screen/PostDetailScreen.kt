package com.example.bloom.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bloom.data.StoryData
import com.example.bloom.network.RetrofitInstance
import com.example.bloom.util.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PostDetailScreen(navController: NavController, storyId: Int?) {
    val context = LocalContext.current
    val token = PreferenceManager.getAccessToken() ?: ""
    val bearerToken = "Bearer $token"
    val coroutineScope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    var story by remember { mutableStateOf<StoryData?>(null) }

    if (storyId == null) {
        Toast.makeText(context, "잘못된 Story ID입니다.", Toast.LENGTH_SHORT).show()
        return
    }

    LaunchedEffect(storyId) {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.getStoryById(bearerToken, storyId)
                if (response.isSuccessful) {
                    story = response.body()
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "스토리 불러오기 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "서버 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
        ) {
            story?.let { storyData ->
                // ✅ 이미지 섹션
                storyData.image_url?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "스토리 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // ✅ 감정 / 작성일 (내용 위로 이동)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "감정: ${storyData.emotion_type}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "작성일: ${storyData.created_at.substring(0, 10)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 내용 (감정/작성일 아래로 이동)
                Text(
                    text = storyData.content,
                    fontSize = 18.sp,
                    lineHeight = 26.sp
                )

            } ?: run {
                // ✅ 데이터가 없는 경우 (에러 메시지 처리)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "스토리를 불러올 수 없습니다.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
