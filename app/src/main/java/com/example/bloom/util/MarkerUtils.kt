
package com.example.bloom.util

import com.example.bloom.R

/**
 * 감정에 따라 마커 이미지 리소스 ID를 반환하는 함수
 */
fun getMarkerImageResId(emotion: String): Int {
    return when (emotion) {
        // 기쁨
        "신남" -> R.drawable.flower_7
        "만족" -> R.drawable.flower_7
        "설렘" -> R.drawable.flower_7
        "행복" -> R.drawable.flower_7

        // 슬픔
        "외로움" -> R.drawable.flower_3
        "우울" -> R.drawable.flower_3
        "실망" -> R.drawable.flower_3
        "허무" -> R.drawable.flower_3

        // 놀람
        "당황" -> R.drawable.flower_1
        "경이로움" -> R.drawable.flower_1
        "혼란" -> R.drawable.flower_1
        "신기" -> R.drawable.flower_1

        // 분노
        "짜증" -> R.drawable.flower_2
        "답답" -> R.drawable.flower_2
        "억울" -> R.drawable.flower_2
        "분개" -> R.drawable.flower_2

        // 공포
        "불안" -> R.drawable.flower_4
        "긴장" -> R.drawable.flower_4
        "두려움" -> R.drawable.flower_4
        "겁남" -> R.drawable.flower_4

        // 혐오
        "불쾌" -> R.drawable.flower_9
        "역겨움" -> R.drawable.flower_9
        "거부감" -> R.drawable.flower_9
        "싫증" -> R.drawable.flower_9

        else -> R.drawable.flower_5  // 기본 이미지
    }
}
