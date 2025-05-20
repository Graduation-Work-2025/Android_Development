package com.example.bloom.data

data class GptRequestBody(
    val model: String = "gpt-3.5-turbo",
    val messages: List<MessageData>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 150
)

data class MessageData(
    val role: String,
    val content: String
)

data class GptResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: MessageData
)
