package com.example.bloom.network

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object ImageUploader {
    private val client = OkHttpClient()

    /**
     * presignedUrl 에 파일을 PUT 방식으로 업로드합니다.
     * @return 성공(true) / 실패(false)
     */
    fun uploadImageToS3(presignedUrl: String, file: File): Boolean {
        try {
            val mediaType = "image/png".toMediaType()
            val body = file.asRequestBody(mediaType)

            Log.d("ImageUploader", "이미지 업로드 URL: $presignedUrl")
            Log.d("ImageUploader", "이미지 파일 경로: ${file.path}, 크기: ${file.length()} bytes")

            val request = Request.Builder()
                .url(presignedUrl)
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                Log.d("ImageUploader", "이미지 업로드 응답 코드: ${response.code}")
                Log.d("ImageUploader", "이미지 업로드 응답 메시지: ${response.message}")
                Log.d("ImageUploader", "이미지 업로드 응답 바디: ${response.body?.string()}")

                return response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("ImageUploader", "이미지 업로드 실패: ${e.message}")
            return false
        }
    }
}
