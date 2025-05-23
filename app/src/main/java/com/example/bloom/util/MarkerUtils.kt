package com.example.bloom.util

import android.util.Log
import com.example.bloom.R

data class MarkerImageInfo(val resId: Int, val width: Int, val height: Int)

/**
 * 감정에 따라 마커 이미지 리소스 ID를 반환하는 함수
 */
fun getMarkerImageInfo(emotion: String, isMine: Boolean): MarkerImageInfo {
    Log.e("getMarkerImageInfo", "emotion: $emotion, isMine: $isMine")
    return when (emotion) {
        "기쁨", "신남", "만족", "설렘", "행복" -> if (isMine) MarkerImageInfo(R.drawable.my_flower_7, 150, 150) else MarkerImageInfo(R.drawable.flower_7, 180, 180)
        "슬픔", "외로움", "우울", "실망", "허무" -> if (isMine) MarkerImageInfo(R.drawable.my_flower_3, 245, 245) else MarkerImageInfo(R.drawable.flower_3, 170, 170)
        "놀람", "당황", "경이로움", "혼란" -> if (isMine) MarkerImageInfo(R.drawable.my_flower_1, 150, 150) else MarkerImageInfo(R.drawable.flower_1, 180, 180)
        "분노", "짜증", "답답", "억울", "분개" -> if (isMine) MarkerImageInfo(R.drawable.my_flower_2, 225, 225) else MarkerImageInfo(R.drawable.flower_2, 225, 225)
        "공포", "불안", "긴장", "두려움", "겁남" -> if (isMine) MarkerImageInfo(R.drawable.my_flower_4, 250, 250) else MarkerImageInfo(R.drawable.flower_4, 180, 180)
        "혐오", "불쾌", "역겨움", "거부감", "싫증" -> if (isMine) MarkerImageInfo(R.drawable.my_flower_9, 150, 150) else MarkerImageInfo(R.drawable.flower_9, 150, 150)
        else -> MarkerImageInfo(R.drawable.flower_default, 100, 100)
    }
}