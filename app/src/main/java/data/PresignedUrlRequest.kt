package com.example.bloom.data

data class PresignedUrlRequest(
    val content_length: Long,
    val content_type: String,
    val file_name: String
)
