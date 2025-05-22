package com.example.bloom.screen

import android.Manifest
import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.bloom.R
import com.example.bloom.data.PresignedUrlRequest
import com.example.bloom.data.StoryPostRequest
import com.example.bloom.network.ImageUploader
import com.example.bloom.network.RetrofitInstance
import com.example.bloom.util.TokenProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val emotionCategories = mapOf(
    "기쁨" to listOf("신남", "만족", "설렘", "행복"),
    "슬픔" to listOf("외로움", "우울", "실망", "허무"),
    "놀람" to listOf("당황", "경이로움", "혼란", "신기"),
    "분노" to listOf("짜증", "답답", "억울", "분개"),
    "공포" to listOf("불안", "긴장", "두려움", "겁남"),
    "혐오" to listOf("불쾌", "역겨움", "거부감", "싫증")
)
private val privacyOptions = listOf("PUBLIC", "PRIVATE")

@Composable
fun PostCreateScreen(navController: NavController) {
    val context = LocalContext.current
    val token = TokenProvider.getToken(context) ?: ""
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var postContent by remember { mutableStateOf("") }
    var selectedEmotion by remember { mutableStateOf(emotionCategories.keys.first()) }
    var selectedSubEmotion by remember { mutableStateOf("") }
    var selectedPrivacy by remember { mutableStateOf(privacyOptions[0]) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            selectedImageUri = uri
        }

    // 위치 권한 요청
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            getLocation(fusedLocationClient) { loc ->
                latitude = loc.latitude
                longitude = loc.longitude
            }
        } else {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 권한 요청 실행
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

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
            OutlinedTextField(
                value = postContent,
                onValueChange = { postContent = it },
                label = { Text("내용을 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp)
            )

            EmotionDropdownMenu(
                selectedEmotion = selectedEmotion,
                selectedSubEmotion = selectedSubEmotion,
                subEmotions = emotionCategories[selectedEmotion] ?: listOf(),
                onEmotionSelected = { selectedEmotion = it },
                onSubEmotionSelected = { selectedSubEmotion = it }
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("공개 설정: ")
                privacyOptions.forEach { option ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPrivacy == option,
                            onClick = { selectedPrivacy = option }
                        )
                        Text(option)
                    }
                }
            }

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEFF5E7),
                    contentColor = Color(0xFF004D00)
                )
            ) {
                Text("이미지 선택")
            }

            selectedImageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "선택한 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            // ✅ markerImageResId 선언
            val markerImageResId: Int = when (selectedEmotion) {
                "기쁨" -> R.drawable.flower_7
                "슬픔" -> R.drawable.flower_3
                "놀람" -> R.drawable.flower_1
                "분노" -> R.drawable.flower_2
                "공포" -> R.drawable.flower_4
                "혐오" -> R.drawable.flower_9
                else -> R.drawable.flower_5
            }
            //val markerImage: BitmapDescriptor = getMarkerImageForEmotion(context, selectedEmotion, 100, 100)


            Button(
                onClick = {
                    uploadStory(
                        context = context,
                        navController = navController,
                        token = token,
                        content = postContent,
                        emotion = selectedSubEmotion.ifBlank { selectedEmotion },
                        privacy = selectedPrivacy,
                        imageUri = selectedImageUri,
                        latitude = latitude,
                        longitude = longitude,
                        markerImage = markerImageResId,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55996F))
            ) {
                Text("심기", color = Color.White)
            }
        }
    }
}


private fun getLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(location)
            }
        }
    } catch (e: SecurityException) {
        Log.e("PostCreateScreen", "위치 권한이 허용되지 않았습니다.")
    }
}

private fun uploadStory(
    context: Context,
    navController: NavController,
    token: String,
    content: String,
    emotion: String,
    privacy: String,
    imageUri: Uri?,
    latitude: Double?,
    longitude: Double?,
    markerImage: Int?,
) {
    val bearerToken = "Bearer $token"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            var imageUrl: String? = null

            if (imageUri != null) {
                val imageFile = File(context.cacheDir, "upload.png")
                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    imageFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val presignedRequest = PresignedUrlRequest(
                    content_length = imageFile.length(),
                    content_type = "image/png",
                    file_name = imageFile.name
                )

                val response = RetrofitInstance.api.getPresignedUrl(bearerToken, presignedRequest)
                val presignedUrl = response.body()?.presigned_url
                imageUrl = response.body()?.file_url

                if (!presignedUrl.isNullOrEmpty()) {
                    ImageUploader.uploadImageToS3(presignedUrl, imageFile)
                }
            }

            val storyRequest = StoryPostRequest(
                content = content,
                longitude = longitude ?: 0.0,
                latitude = latitude ?: 0.0,
                sharing_type = privacy,
                emotion_type = emotion,
                image_url = imageUrl,
                marker_image = markerImage
            )

            val response = RetrofitInstance.api.postStory(bearerToken, storyRequest)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    Log.e("PostCreateScreen", "스토리 업로드 성공: ${response.body()}")
                } else {
                    Log.e("PostCreateScreen", "스토리 업로드 실패: ${response.errorBody()?.string()}")
                }

                if (response.isSuccessful) {
                    Toast.makeText(context, "스토리가 업로드되었습니다.", Toast.LENGTH_SHORT).show()

                    if (response.body() != null && response.body()!!.remind_story != null) {
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "result_key",
                            "result"
                        )
                        Log.e(
                            "PostCreateScreen",
                            "과거에 근처에서 작성한 스토리가 있어요 ${response.body()!!.remind_story}"
                        )
                    }
                    navController.popBackStack()
                }
            }

        } catch (e: Exception) {
            Log.e("PostCreateScreen", "에러: ${e.message}")
        }
    }
}

// ✅ EmotionDropdownMenu 함수 수정
@Composable
fun EmotionDropdownMenu(
    selectedEmotion: String,
    selectedSubEmotion: String,
    subEmotions: List<String>,
    onEmotionSelected: (String) -> Unit,
    onSubEmotionSelected: (String) -> Unit
) {
    var showEmotionDialog by remember { mutableStateOf(false) }
    var showSubEmotionDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 감정 선택 버튼
        OutlinedButton(
            onClick = { showEmotionDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Text(text = selectedEmotion)
        }

        if (showEmotionDialog) {
            AlertDialog(
                onDismissRequest = { showEmotionDialog = false },
                title = { Text("감정 선택") },
                text = {
                    val emotions = emotionCategories.keys.toList()
                    // 4열 그리드
                    val rows = (emotions.size + 3) / 4
                    Column {
                        for (row in 0 until rows) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0 until 4) {
                                    val idx = row * 4 + col
                                    if (idx < emotions.size) {
                                        val emotion = emotions[idx]
                                        TextButton(
                                            onClick = {
                                                onEmotionSelected(emotion)
                                                onSubEmotionSelected("")
                                                showEmotionDialog = false
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(2.dp)
                                        ) {
                                            Text(emotion)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        // 세부 감정 선택 버튼
        OutlinedButton(
            onClick = { showSubEmotionDialog = true },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (selectedSubEmotion.isNotBlank()) selectedSubEmotion else "세부 감정 선택"
            )
        }

        if (showSubEmotionDialog) {
            AlertDialog(
                onDismissRequest = { showSubEmotionDialog = false },
                title = { Text("세부 감정 선택") },
                text = {
                    val rows = (subEmotions.size + 3) / 4
                    Column {
                        for (row in 0 until rows) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0 until 4) {
                                    val idx = row * 4 + col
                                    if (idx < subEmotions.size) {
                                        val subEmotion = subEmotions[idx]
                                        TextButton(
                                            onClick = {
                                                onSubEmotionSelected(subEmotion)
                                                showSubEmotionDialog = false
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(2.dp)
                                        ) {
                                            Text(subEmotion)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}
