package com.example.bloom.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bloom.R
import com.example.bloom.components.BackButton
import com.example.bloom.data.SignUpRequestDto
import com.example.bloom.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.Arrangement

@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val context = LocalContext.current
    val defaultCharacterId = 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top  // ✅ 위쪽으로 정렬
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Image(
            painter = painterResource(id = R.drawable.register),
            contentDescription = "회원가입 타이틀",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))  // ✅ 간격 조정

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            modifier = Modifier.fillMaxWidth(0.85f),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("닉네임") },
            modifier = Modifier.fillMaxWidth(0.85f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userId,
            onValueChange = { userId = it },
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth(0.85f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth(0.85f),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("전화번호") },
            modifier = Modifier.fillMaxWidth(0.85f),
        )

        Spacer(modifier = Modifier.height(20.dp))  // ✅ 간격 줄이기

        Button(
            onClick = {
                if (name.isBlank() || nickname.isBlank() || userId.isBlank() || password.isBlank() || phone.isBlank()) {
                    Toast.makeText(context, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val dto = SignUpRequestDto(
                    name = name,
                    nickname = nickname,
                    user_id = userId,
                    password = password,
                    phone = phone,
                    character_id = defaultCharacterId
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitInstance.api.signUp(dto)

                        if (response.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                                navController.navigate("login") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "회원가입 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.size(250.dp, 45.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55996F))
        ) {
            Text("회원가입", fontSize = 18.sp, color = Color.White)
        }
    }
}
