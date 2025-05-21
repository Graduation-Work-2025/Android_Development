package com.example.bloom.util

import com.example.bloom.R

/**
 * 감정에 따라 마커 이미지 리소스 ID를 반환하는 함수
 */
fun getMarkerImageResId(emotion: String, is_mine: Boolean): Int {
    return when (emotion) {
        "신남", "만족", "설렘", "행복" -> if (is_mine) R.drawable.my_flower_7 else R.drawable.flower_7
        "외로움", "우울", "실망", "허무" -> if (is_mine) R.drawable.my_flower_3 else R.drawable.flower_3
        "당황", "경이로움", "혼란" -> if (is_mine) R.drawable.my_flower_1 else R.drawable.flower_1
        "짜증", "답답", "억울", "분개" -> if (is_mine) R.drawable.my_flower_2 else R.drawable.flower_2
        "불안", "긴장", "두려움", "겁남" -> if (is_mine) R.drawable.my_flower_4 else R.drawable.flower_4
        "불쾌", "역겨움", "거부감", "싫증" -> if (is_mine) R.drawable.my_flower_9 else R.drawable.flower_9
        else -> if (is_mine) R.drawable.my_flower_5 else R.drawable.flower_5 // 기본 이미지
    }
}
