package com.example.bloom.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bloom.R
import com.example.bloom.data.StoryData
import com.example.bloom.network.RetrofitInstance
import com.example.bloom.util.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MyFeedGardenScreen(navController: NavController) {
    val context = LocalContext.current
    val token = PreferenceManager.getAccessToken() ?: ""
    val bearerToken = "Bearer $token"
    val coroutineScope = rememberCoroutineScope()

    var stories by remember { mutableStateOf(emptyList<StoryData>()) }
    var nickname by remember { mutableStateOf(PreferenceManager.getNickname()) }

    // ✅ 게시물 목록 불러오기 (최신순 정렬)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.getMyStories(bearerToken)
                if (response.isSuccessful) {
                    val fetchedStories = response.body()?.stories ?: emptyList()
                    stories = fetchedStories.sortedByDescending { it.created_at }
                }
            } catch (e: Exception) {
                Log.e("MyFeedGardenScreen", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "스토리 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {

            // ✅ 상단 프로필 섹션
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile_default),
                    contentDescription = "프로필 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(1.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = nickname,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ✅ 구분선 추가
            Divider(
                color = Color(0xFF55996F), // 초록색
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)  // 위아래 패딩 설정
            )

            if (stories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "아직 업로드된 게시물이 없습니다.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(stories.sortedByDescending { it.created_at }) { story ->
                        val imageUrl = story.image_url ?: ""

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(Color.White)
                                .padding(1.dp)
                                .clickable {
                                    navController.navigate("post_detail/${story.id}")
                                }
                        ) {
                            if (imageUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = "Story Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "이미지 없음",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground
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

