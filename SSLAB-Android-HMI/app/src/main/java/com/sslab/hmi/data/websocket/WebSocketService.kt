package com.sslab.hmi.data.websocket

import android.util.Log
import com.google.gson.Gson
import com.sslab.hmi.data.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket通信服务
 */
@Singleton
class WebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    
    companion object {
        private const val TAG = "WebSocketService"
        private const val WS_URL = "ws://localhost:3000/ws"
    }
    
    private var webSocket: WebSocket? = null
    private var isConnected = false
    
    // 连接状态流
    private val _connectionState = MutableSharedFlow<ConnectionState>()
    val connectionState: SharedFlow<ConnectionState> = _connectionState.asSharedFlow()
    
    // 设备状态更新流
    private val _deviceUpdates = MutableSharedFlow<DeviceUpdate>()
    val deviceUpdates: SharedFlow<DeviceUpdate> = _deviceUpdates.asSharedFlow()
    
    // 教学电源数据流
    private val _teachingPowerUpdates = MutableSharedFlow<TeachingPower>()
    val teachingPowerUpdates: SharedFlow<TeachingPower> = _teachingPowerUpdates.asSharedFlow()
    
    // 环境数据流
    private val _environmentUpdates = MutableSharedFlow<EnvironmentData>()
    val environmentUpdates: SharedFlow<EnvironmentData> = _environmentUpdates.asSharedFlow()
    
    // 升降台数据流
    private val _liftUpdates = MutableSharedFlow<LiftControl>()
    val liftUpdates: SharedFlow<LiftControl> = _liftUpdates.asSharedFlow()
    
    /**
     * 连接WebSocket
     */
    fun connect() {
        if (isConnected) {
            Log.d(TAG, "WebSocket already connected")
            return
        }
        
        val request = Request.Builder()
            .url(WS_URL)
            .build()
        
        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
    }
    
    /**
     * 断开WebSocket连接
     */
    fun disconnect() {
        webSocket?.close(1000, "Client closing")
        webSocket = null
        isConnected = false
    }
    
    /**
     * 发送消息
     */
    fun sendMessage(message: WebSocketMessage) {
        val json = gson.toJson(message)
        webSocket?.send(json)
        Log.d(TAG, "Sent message: $json")
    }
    
    /**
     * 发送控制命令
     */
    fun sendControlCommand(deviceId: String, command: String, parameters: Map<String, Any> = emptyMap()) {
        val message = WebSocketMessage(
            type = MessageType.CONTROL_COMMAND,
            deviceId = deviceId,
            data = mapOf(
                "command" to command,
                "parameters" to parameters,
                "timestamp" to System.currentTimeMillis()
            )
        )
        sendMessage(message)
    }
    
    /**
     * WebSocket监听器
     */
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connected")
            isConnected = true
            _connectionState.tryEmit(ConnectionState.CONNECTED)
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received message: $text")
            handleMessage(text)
        }
        
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "Received bytes: ${bytes.hex()}")
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code $reason")
            _connectionState.tryEmit(ConnectionState.DISCONNECTING)
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code $reason")
            isConnected = false
            _connectionState.tryEmit(ConnectionState.DISCONNECTED)
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket error", t)
            isConnected = false
            _connectionState.tryEmit(ConnectionState.ERROR)
        }
    }
    
    /**
     * 处理接收到的消息
     */
    private fun handleMessage(text: String) {
        try {
            val message = gson.fromJson(text, WebSocketMessage::class.java)
            
            when (message.type) {
                MessageType.DEVICE_STATUS -> {
                    val deviceUpdate = gson.fromJson(gson.toJson(message.data), DeviceUpdate::class.java)
                    _deviceUpdates.tryEmit(deviceUpdate)
                }
                
                MessageType.TEACHING_POWER -> {
                    val powerData = gson.fromJson(gson.toJson(message.data), TeachingPower::class.java)
                    _teachingPowerUpdates.tryEmit(powerData)
                }
                
                MessageType.ENVIRONMENT_DATA -> {
                    val envData = gson.fromJson(gson.toJson(message.data), EnvironmentData::class.java)
                    _environmentUpdates.tryEmit(envData)
                }
                
                MessageType.LIFT_STATUS -> {
                    val liftData = gson.fromJson(gson.toJson(message.data), LiftControl::class.java)
                    _liftUpdates.tryEmit(liftData)
                }
                
                else -> {
                    Log.d(TAG, "Unhandled message type: ${message.type}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message", e)
        }
    }
}

/**
 * WebSocket消息数据类
 */
data class WebSocketMessage(
    val type: MessageType,
    val deviceId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val data: Any? = null
)

/**
 * 消息类型枚举
 */
enum class MessageType {
    DEVICE_STATUS,
    TEACHING_POWER,
    ENVIRONMENT_DATA,
    LIFT_STATUS,
    CONTROL_COMMAND,
    HEARTBEAT,
    ERROR
}

/**
 * 连接状态枚举
 */
enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    DISCONNECTED,
    ERROR
}

/**
 * 设备更新数据类
 */
data class DeviceUpdate(
    val deviceId: String,
    val status: DeviceStatus,
    val lastSeen: Long = System.currentTimeMillis()
)
