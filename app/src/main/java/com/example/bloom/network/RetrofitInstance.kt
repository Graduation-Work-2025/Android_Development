package com.example.bloom.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object RetrofitInstance {

    private const val BASE_URL = "https://bloom-story.kro.kr/"

    /**
     * Retrofit API 인스턴스
     */
    val api: ApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("RetrofitInstance", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val headerInterceptor = Interceptor { chain ->
            val request: Request = chain.request()

            // 요청 로그
            Log.d("RetrofitInstance", "Request URL: ${request.url}")
            Log.d("RetrofitInstance", "Request Headers: ${request.headers}")
            Log.d("RetrofitInstance", "Request Method: ${request.method}")

            val response: Response = chain.proceed(request)

            // 응답 로그
            Log.d("RetrofitInstance", "Response Code: ${response.code}")
            Log.d("RetrofitInstance", "Response Message: ${response.message}")

            return@Interceptor response
        }

        val errorHandlingInterceptor = Interceptor { chain ->
            try {
                val response = chain.proceed(chain.request())
                if (!response.isSuccessful) {
                    Log.e("RetrofitInstance", "HTTP Error: ${response.code} - ${response.message}")
                }
                response
            } catch (e: IOException) {
                Log.e("RetrofitInstance", "Network Error: ${e.message}")
                throw e
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(headerInterceptor)
            .addInterceptor(errorHandlingInterceptor)
            .build()

        return@lazy Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}
