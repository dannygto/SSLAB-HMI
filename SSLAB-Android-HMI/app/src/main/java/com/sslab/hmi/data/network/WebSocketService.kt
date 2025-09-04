package com.sslab.hmi.data.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.sslab.hmi.data.model.DeviceStatusUpdate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket实时通信服务
 * 用于接收设备状态实时更新
 */
@Singleton
class WebSocketService @Inject constructor(
    private val gson: Gson
) {
    companion object {
        private const val TAG = "WebSocketService"
        private const val RECONNECT_DELAY = 5000L // 5秒重连延迟
    }
    
    private var webSocketClient: WebSocketClient? = null
    private var isConnected = false
    private var serverUrl = ""
    
    // 设备状态更新流
    private val _deviceStatusUpdates = MutableSharedFlow<DeviceStatusUpdate>()
    val deviceStatusUpdates: SharedFlow<DeviceStatusUpdate> = _deviceStatusUpdates.asSharedFlow()
    
    // 连接状态流
    private val _connectionStatus = MutableSharedFlow<Boolean>()
    val connectionStatus: SharedFlow<Boolean> = _connectionStatus.asSharedFlow()
    
    // 错误消息流
    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages: SharedFlow<String> = _errorMessages.asSharedFlow()
    
    /**
     * 连接到WebSocket服务器
     */
    fun connect(baseUrl: String) {
        if (isConnected) {
            Log.d(TAG, "Already connected to WebSocket")
            return
        }
        
        serverUrl = baseUrl
        val wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://")
        val uri = URI.create("$wsUrl/ws")
        
        Log.d(TAG, "Connecting to WebSocket: $uri")
        
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshake: ServerHandshake?) {
                Log.d(TAG, "WebSocket connected")
                isConnected = true
                _connectionStatus.tryEmit(true)
            }
            
            override fun onMessage(message: String?) {
                Log.d(TAG, "Received WebSocket message: $message")
                message?.let { handleMessage(it) }
            }
            
            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "WebSocket closed. Code: $code, Reason: $reason, Remote: $remote")
                isConnected = false
                _connectionStatus.tryEmit(false)
                
                // 自动重连
                if (remote) {
                    scheduleReconnect()
                }
            }
            
            override fun onError(ex: Exception?) {
                Log.e(TAG, "WebSocket error", ex)
                _errorMessages.tryEmit(ex?.message ?: "Unknown WebSocket error")
            }
        }
        
        webSocketClient?.connect()
    }
    
    /**
     * 断开WebSocket连接
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        webSocketClient?.close()
        webSocketClient = null
        isConnected = false
        _connectionStatus.tryEmit(false)
    }
    
    /**
     * 发送消息到服务器
     */
    fun sendMessage(message: String) {
        if (isConnected) {
            webSocketClient?.send(message)
            Log.d(TAG, "Sent WebSocket message: $message")
        } else {
            Log.w(TAG, "Cannot send message, WebSocket not connected")
        }
    }
    
    /**
     * 订阅设备状态更新
     */
    fun subscribeToDevice(deviceId: String) {
        val subscribeMessage = gson.toJson(mapOf(
            "action" to "subscribe",
            "deviceId" to deviceId
        ))
        sendMessage(subscribeMessage)
    }
    
    /**
     * 取消订阅设备状态更新
     */
    fun unsubscribeFromDevice(deviceId: String) {
        val unsubscribeMessage = gson.toJson(mapOf(
            "action" to "unsubscribe",
            "deviceId" to deviceId
        ))
        sendMessage(unsubscribeMessage)
    }
    
    /**
     * 处理接收到的消息
     */
    private fun handleMessage(message: String) {
        try {
            val messageData = gson.fromJson(message, Map::class.java)
            when (messageData["type"]) {
                "deviceStatusUpdate" -> {
                    val update = gson.fromJson(message, DeviceStatusUpdate::class.java)
                    _deviceStatusUpdates.tryEmit(update)
                }
                "error" -> {
                    val errorMsg = messageData["message"] as? String ?: "Unknown error"
                    _errorMessages.tryEmit(errorMsg)
                }
                else -> {
                    Log.d(TAG, "Unknown message type: ${messageData["type"]}")
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse WebSocket message: $message", e)
        }
    }
    
    /**
     * 安排重连
     */
    private fun scheduleReconnect() {
        Log.d(TAG, "Scheduling WebSocket reconnect in ${RECONNECT_DELAY}ms")
        
        Thread {
            Thread.sleep(RECONNECT_DELAY)
            if (!isConnected && serverUrl.isNotEmpty()) {
                Log.d(TAG, "Attempting WebSocket reconnect")
                connect(serverUrl)
            }
        }.start()
    }
    
    /**
     * 检查连接状态
     */
    fun isConnected(): Boolean = isConnected
}
