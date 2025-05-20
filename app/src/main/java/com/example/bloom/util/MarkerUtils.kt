package com.example.bloom.util

import com.example.bloom.R

/**
 * 감정에 따라 마커 이미지 리소스 ID를 반환하는 함수
 */
fun getMarkerImageResId(emotion: String): Int {
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
