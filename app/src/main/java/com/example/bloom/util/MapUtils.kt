package com.example.bloom.util

import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun getColorForEmotion(emotion: String): Float {
    return when (emotion) {
        "기쁨" -> BitmapDescriptorFactory.HUE_YELLOW
        "슬픔" -> BitmapDescriptorFactory.HUE_BLUE
        "놀람" -> BitmapDescriptorFactory.HUE_ORANGE
        "분노" -> BitmapDescriptorFactory.HUE_RED
        "공포" -> BitmapDescriptorFactory.HUE_VIOLET
        "혐오" -> BitmapDescriptorFactory.HUE_GREEN
        else -> BitmapDescriptorFactory.HUE_ROSE
    }
}
