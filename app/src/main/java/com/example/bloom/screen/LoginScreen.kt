package com.example.bloom.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloom.R
import com.example.bloom.data.LoginRequestDto
import com.example.bloom.network.RetrofitInstance
import com.example.bloom.util.PreferenceManager
import com.example.bloom.util.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(navController: NavController) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Image(
            painter = painterResource(id = R.drawable.join),
            contentDescription = "로그인 이미지",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth(0.85f),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(0.85f),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                if (userId.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val loginDto = LoginRequestDto(user_id = userId, password = password)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.api.login(loginDto)
                        Log.d("LoginScreen", "로그인 요청: $loginDto")

                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            val accessToken = responseBody?.access_token ?: ""
                            val receivedUserId = responseBody?.user_id ?: -1

                            Log.d("LoginScreen", "받은 토큰: $accessToken, 유저 ID: $receivedUserId")

                            if (accessToken.isNotBlank() && receivedUserId != -1) {
                                // ✅ Access Token 저장
                                PreferenceManager.setAccessToken(accessToken)
                                TokenProvider.setToken(accessToken, context)

                                // ✅ 유저 ID 저장
                                PreferenceManager.setUserId(receivedUserId)

                                // ✅ 사용자 정보 가져오기

                                val userInfoResponse = RetrofitInstance.api.getMyInfo(
                                    token = "Bearer $accessToken"
                                )


                                if (userInfoResponse.isSuccessful) {
                                    val userInfo = userInfoResponse.body()
                                    userInfo?.let { user ->
                                        val nickname = user.nickname ?: "사용자"
                                        Log.d("LoginScreen", "받은 닉네임: $nickname")

                                        // ✅ 닉네임 저장
                                        PreferenceManager.setNickname(nickname)
                                    }
                                } else {
                                    Log.e("LoginScreen", "사용자 정보 불러오기 실패: ${userInfoResponse.code()}")
                                }

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "서버에서 유효한 데이터가 반환되지 않았습니다.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val errorMessage = when (response.code()) {
                                401 -> "아이디 또는 비밀번호가 잘못되었습니다."
                                else -> "로그인 실패: ${response.code()}"
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("LoginScreen", "로그인 오류: ${e.message}")
                    }
                }
            },
            modifier = Modifier.size(250.dp, 45.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55996F))
        ) {
            Text("로그인", fontSize = 18.sp, color = Color.White)
        }
    }
}
