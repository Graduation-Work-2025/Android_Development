package com.example.bloom.network

import android.util.Log
import io.reactivex.disposables.Disposable
import okhttp3.*
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.concurrent.TimeUnit

object WebSocketManager {

    private const val SOCKET_URL = "wss://bloom-story.kro.kr/ws/unity"
    private const val TAG = "WebSocketManager"

    private var stompClient: StompClient? = null
    private var lifecycleDisposable: Disposable? = null
    private var storySubscription: Disposable? = null

    /**
     * WebSocket 연결
     */
    fun connectStomp(token: String) {
        if (stompClient?.isConnected == true) return

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SOCKET_URL)
        lifecycleDisposable = stompClient?.lifecycle()?.subscribe { event ->
            when (event.type) {
                LifecycleEvent.Type.OPENED -> Log.d(TAG, "STOMP 연결됨")
                LifecycleEvent.Type.ERROR -> Log.e(TAG, "STOMP 오류: ${event.exception}")
                LifecycleEvent.Type.CLOSED -> Log.d(TAG, "STOMP 연결 종료됨")
                LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> Log.w(TAG, "STOMP 핑 실패")
            }
        }

        stompClient?.connect(
            listOf(ua.naiksoftware.stomp.dto.StompHeader("Authorization", "Bearer $token"))
        )
    }

    /**
     * 위치 기반 스토리 요청
     */
    fun requestNearbyStories(token: String, longitude: Double, latitude: Double) {
        val message = """
            {
                "domain": "story",
                "command": "get_stories",
                "token": "$token",
                "request": {
                    "longitude": $longitude,
                    "latitude": $latitude
                }
            }
        """.trimIndent()

        sendStompMessage("/app/story", message)
        Log.d(TAG, "위치 기반 스토리 요청: $message")
    }

    /**
     * STOMP 메시지 전송
     */
    fun sendStompMessage(destination: String, message: String) {
        stompClient?.send(destination, message)?.subscribe({
            Log.d(TAG, "메시지 전송 성공")
        }, { error ->
            Log.e(TAG, "메시지 전송 실패: ${error.message}")
        })
    }

    /**
     * STOMP 구독
     */
    fun subscribeToNearbyStories(onMessageReceived: (String) -> Unit) {
        storySubscription = stompClient?.topic("/user/queue/story")
            ?.subscribe({ stompMessage ->
                Log.d(TAG, "STOMP 수신: ${stompMessage.payload}")
                onMessageReceived(stompMessage.payload)
            }, { error ->
                Log.e(TAG, "STOMP 구독 오류: ${error.message}")
            })
    }

    /**
     * STOMP 연결 해제
     */
    fun disconnectStomp() {
        lifecycleDisposable?.dispose()
        storySubscription?.dispose()
        stompClient?.disconnect()
        stompClient = null
        Log.d(TAG, "STOMP 연결 해제됨")
    }
}
