package com.example.bloom.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.bloom.R
import com.example.bloom.data.FeedFlower
import com.example.bloom.network.RetrofitInstance
import com.example.bloom.util.TokenProvider
import com.example.bloom.util.getMarkerImageInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

private var locationCallback: LocationCallback? = null

/**
 * 마커 아이콘의 크기를 조정하는 함수
 * @param context - Context
 * @param resourceId - 이미지 리소스 ID
 * @param width - 원하는 너비 (픽셀 단위)
 * @param height - 원하는 높이 (픽셀 단위)
 */
fun getResizedMarkerIcon(
    context: Context,
    resourceId: Int,
    width: Int,
    height: Int
): BitmapDescriptor {
    val originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
    val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    return BitmapDescriptorFactory.fromBitmap(resizedBitmap)
}

/**
 * 꽃들을 근접한 위치로 그룹화하는 함수
 * @param flowers - FeedFlower 리스트
 * @param maxDistance - 최대 거리 (라디안 단위)
 * @return 그룹화된 LatLng 리스트와 개별 LatLng 리스트
 */
fun groupFlowersByProximity(
    flowers: List<FeedFlower>,
    maxDistance: Double
): Pair<List<Pair<LatLng, List<FeedFlower>>>, List<Pair<LatLng, FeedFlower>>> {
    val groupedFlowers = mutableListOf<Pair<LatLng, MutableList<FeedFlower>>>()
    val individualFlowers = mutableListOf<Pair<LatLng, FeedFlower>>()

    flowers.forEach { flower ->
        val flowerPosition = LatLng(flower.latitude, flower.longitude)
        val existingGroup = groupedFlowers.find { group ->
            val distance = sqrt(
                (flowerPosition.latitude - group.first.latitude).pow(2) +
                        (flowerPosition.longitude - group.first.longitude).pow(2)
            )
            distance <= maxDistance
        }

        if (existingGroup != null) {
            existingGroup.second.add(flower)
        } else {
            groupedFlowers.add(Pair(flowerPosition, mutableListOf(flower)))
        }
    }

    groupedFlowers.forEach { group ->
        if (group.second.size == 1) {
            individualFlowers.add(Pair(group.first, group.second.first()))
        }
    }

    val finalGroupedFlowers = groupedFlowers.filter { it.second.size > 1 }

    return Pair(
        finalGroupedFlowers.map { Pair(it.first, it.second.toList()) },
        individualFlowers
    )
}

@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    var feedList by remember { mutableStateOf<List<FeedFlower>>(emptyList()) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var showRemindDialog by remember { mutableStateOf(false) }

    // ✅ 초기값을 한국 대전 위치로 설정
    val initialLocation = LatLng(36.7631, 127.2827)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 13f)
    }

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    val token = TokenProvider.getToken(context) ?: ""
    val bearerToken = "Bearer $token"

    val requestLocationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates(fusedLocationClient) { location ->
                val newLocation = LatLng(location.latitude, location.longitude)
                userLocation = newLocation

                // ✅ 위치가 업데이트되면 카메라 이동
                coroutineScope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(newLocation, 17f),
                        durationMs = 1000
                    )
                }
            }
        } else {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(bearerToken, userLocation) {
        coroutineScope.launch {
            userLocation?.let { location ->
                feedList = fetchFeedFlowers(bearerToken, location.latitude, location.longitude)
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController, currentRoute = currentRoute)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                userLocation?.let { location ->
                    Marker(
                        state = MarkerState(location),
                        title = "내 위치",
                        icon = getResizedMarkerIcon(context, R.drawable.ch0, 150, 150),
                        onClick = { true }
                    )
                }

                val (groupedMarkers, individualMarkers) = groupFlowersByProximity(
                    feedList,
                    0.00005
                ) // Approx. 5m in lat/lng degrees

                groupedMarkers.forEach { markerPosition ->
                    Marker(
                        state = MarkerState(markerPosition.first),
                        icon = getResizedMarkerIcon(
                            context,
                            R.drawable.flower_8,
                            150,
                            150
                        ),
                        title = "스토리 그룹",
                        onClick = {
                            navController.navigate("post_list/${markerPosition.second[0].id}")
                            true
                        }
                    )
                }

                individualMarkers.forEach { markerPosition ->
                    val markerImageInfo = getMarkerImageInfo(markerPosition.second.emotion, markerPosition.second.is_mine)
                    Marker(
                        state = MarkerState(markerPosition.first),
                        icon = getResizedMarkerIcon(
                            context,
                            markerImageInfo.resId,
                            markerImageInfo.width,
                            markerImageInfo.height
                        ),
                        title = "스토리",
                        onClick = {
                            navController.navigate("post_detail/${markerPosition.second.id}")
                            true
                        }
                    )
                }
            }

            FloatingActionButton(
                onClick = {
                    navController.navigate("create_post")
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Text("+", color = Color.White, fontSize = 24.sp)
            }
            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
            val result = savedStateHandle?.getLiveData<String>("result_key")?.observeAsState()
            result?.value?.let { resultValue ->
                if (resultValue.isNotEmpty()) {
                    // 결과값을 사용하여 UI 업데이트
                    savedStateHandle.set("result_key", "") // 결과값 초기화
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1000)
                        showRemindDialog = true
                    }
                }
            }
            if (showRemindDialog) {
                AlertDialog(
                    onDismissRequest = { showRemindDialog = false },
                    title = { Text("알림") },
                    text = { Text("과거에 근처에서 작성한 스토리가 있어요") },
                    confirmButton = {
                        TextButton(onClick = {
                            showRemindDialog = false
                        }) {
                            Text("확인")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, currentRoute: String?) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier.height(70.dp)  // 높이 조정
    ) {
        NavigationBarItem(
            selected = currentRoute == "main",
            onClick = { navController.navigate("main") { launchSingleTop = true } },
            icon = {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "홈",
                    modifier = Modifier.offset(y = 4.dp)  // 아이콘 아래로 4dp 내리기
                )
            },
            label = {
                Text(
                    "홈",
                    color = if (currentRoute == "main") Color(0xFF55996F) else Color.Gray,
                    modifier = Modifier.offset(y = 2.dp)  // 텍스트 아래로 2dp 내리기
                )
            }
        )

        NavigationBarItem(
            selected = currentRoute == "chatgpttest",
            onClick = { navController.navigate("chatgpttest") { launchSingleTop = true } },
            icon = {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "리포트",
                    modifier = Modifier.offset(y = 4.dp)
                )
            },
            label = {
                Text(
                    "리포트",
                    color = if (currentRoute == "chatgpttest") Color(0xFF55996F) else Color.Gray,
                    modifier = Modifier.offset(y = 2.dp)
                )
            }
        )

        NavigationBarItem(
            selected = currentRoute == "emotion_garden",
            onClick = { navController.navigate("emotion_garden") { launchSingleTop = true } },
            icon = {
                Icon(
                    Icons.Filled.Spa,
                    contentDescription = "정원",
                    modifier = Modifier.offset(y = 4.dp)
                )
            },
            label = {
                Text(
                    "정원",
                    color = if (currentRoute == "emotion_garden") Color(0xFF55996F) else Color.Gray,
                    modifier = Modifier.offset(y = 2.dp)
                )
            }
        )
    }
}


@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
private fun startLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    val locationRequest = LocationRequest.create().apply {
        interval = 3000
        fastestInterval = 1500
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { onLocationReceived(it) }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback!!,
        Looper.getMainLooper()
    )
}

suspend fun fetchFeedFlowers(
    bearerToken: String,
    latitude: Double,
    longitude: Double
): List<FeedFlower> {
    return try {
        val response = RetrofitInstance.api.getStories(bearerToken, latitude, longitude)
        if (response.isSuccessful) {
            response.body()?.stories?.map { story ->
                FeedFlower(
                    id = story.id,
                    latitude = story.latitude,
                    longitude = story.longitude,
                    emotion = story.emotion_type,
                    is_mine = story.is_mine,
                )
            } ?: emptyList()
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}
/*
fun getMarkerImageForEmotion(emotion: String): Int {
    return when (emotion) {
        "기쁨" -> R.drawable.flower_7
        "슬픔" -> R.drawable.flower_3
        "놀람" -> R.drawable.flower_1
        "분노" -> R.drawable.flower_2
        "공포" -> R.drawable.flower_4
        "혐오" -> R.drawable.flower_9
        else -> R.drawable.flower_5
    }
}
*/

