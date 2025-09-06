package com.sslab.hmi.data.repository

import android.util.Log
import com.sslab.hmi.data.model.*
import com.sslab.hmi.data.model.ApiResponse
import com.sslab.hmi.data.network.SSLabApiService
import com.sslab.hmi.data.network.WebSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设备数据仓库
 * 管理设备相关的所有数据操作
 */
@Singleton
class DeviceRepository @Inject constructor(
    private val apiService: SSLabApiService,
    private val webSocketService: WebSocketService
) {
    companion object {
        private const val TAG = "DeviceRepository"
        
        // 智能选择默认服务器地址
        val DEFAULT_SERVER_URL = run {
            val isEmulator = (android.os.Build.FINGERPRINT.startsWith("generic")
                    || android.os.Build.FINGERPRINT.contains("sdk")
                    || android.os.Build.FINGERPRINT.contains("emulator")
                    || android.os.Build.MODEL.contains("Emulator")
                    || android.os.Build.MODEL.contains("Android SDK")
                    || android.os.Build.DEVICE.contains("generic")
                    || android.os.Build.PRODUCT.contains("sdk")
                    || android.os.Build.PRODUCT.contains("emulator"))
            
            if (isEmulator) {
                "http://10.0.2.2:8080"  // 模拟器访问宿主机
            } else {
                "http://192.168.0.145:8080"  // 物理设备访问实际IP
            }
        }
    }
    
    // 协程作用域
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // 设备列表状态
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 服务器连接状态
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    // 服务器URL
    private var serverUrl = DEFAULT_SERVER_URL
    
    init {
        // 监听WebSocket连接状态
        observeWebSocketStatus()
        // 监听设备状态更新
        observeDeviceStatusUpdates()
    }
    
    /**
     * 设置服务器URL并连接
     */
    fun setServerUrl(url: String) {
        serverUrl = url
        connectToServer()
    }
    
    /**
     * 连接到服务器
     */
    fun connectToServer() {
        Log.d(TAG, "Connecting to server: $serverUrl")
        webSocketService.connect(serverUrl)
        repositoryScope.launch {
            loadDevices()
        }
    }
    
    /**
     * 断开服务器连接
     */
    fun disconnectFromServer() {
        Log.d(TAG, "Disconnecting from server")
        webSocketService.disconnect()
        _isConnected.value = false
    }
    
    /**
     * 加载所有设备
     */
    suspend fun loadDevices() {
        _isLoading.value = true
        _errorMessage.value = null
        
        try {
            val response = apiService.getDevices()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    _devices.value = apiResponse.data
                    Log.d(TAG, "Loaded ${_devices.value.size} devices")
                } else {
                    _errorMessage.value = (apiResponse as? com.sslab.hmi.data.model.ApiResponse<*>)?.error ?: "Failed to load devices"
                }
            } else {
                _errorMessage.value = "HTTP ${response.code()}: ${response.message()}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading devices", e)
            _errorMessage.value = e.message ?: "Unknown error occurred"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * 获取所有设备列表
     */
    suspend fun getAllDevices(): List<Device> {
        return try {
            val response = apiService.getDevices()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    apiResponse.data
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all devices", e)
            emptyList()
        }
    }
    
    /**
     * 添加新设备
     */
    suspend fun addDevice(device: Device): Result<Device> {
        return try {
            val request = AddDeviceRequest(
                name = device.name,
                type = device.type,
                groupId = device.groupId.ifEmpty { null },
                ipAddress = device.ipAddress.ifEmpty { null },
                port = device.port
            )
            val response = apiService.addDevice(request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    // 更新本地设备列表
                    _devices.value = _devices.value + apiResponse.data
                    Log.d(TAG, "Added device: ${device.name}")
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception((apiResponse as? com.sslab.hmi.data.model.ApiResponse<*>)?.error ?: "Failed to add device"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding device", e)
            Result.failure(e)
        }
    }
    
    /**
     * 删除设备
     */
    suspend fun deleteDevice(deviceId: String): Result<Unit> {
        return try {
            val response = apiService.deleteDevice(deviceId)
            if (response.isSuccessful) {
                // 从本地列表中移除
                _devices.value = _devices.value.filter { it.id != deviceId }
                Log.d(TAG, "Deleted device: $deviceId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting device: $deviceId", e)
            Result.failure(e)
        }
    }
    
    /**
     * 发送设备控制命令
     */
    suspend fun sendDeviceCommand(deviceId: String, command: DeviceCommand): Result<Map<String, Any>> {
        return try {
            val response = apiService.sendDeviceCommand(deviceId, command)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Log.d(TAG, "Sent command to device $deviceId: ${command.command}")
                    Result.success(apiResponse.data ?: emptyMap())
                } else {
                    Result.failure(Exception((apiResponse as? com.sslab.hmi.data.model.ApiResponse<*>)?.error ?: "Failed to send command"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending device command", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取环境监测数据
     */
    suspend fun getEnvironmentData(deviceId: String): Result<EnvironmentData> {
        return try {
            val response = apiService.getEnvironmentData(deviceId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception((apiResponse as? com.sslab.hmi.data.model.ApiResponse<*>)?.error ?: "Failed to get environment data"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting environment data", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取电源控制数据
     */
    suspend fun getPowerData(deviceId: String): Result<PowerControlData> {
        return try {
            val response = apiService.getPowerData(deviceId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception((apiResponse as? com.sslab.hmi.data.model.ApiResponse<*>)?.error ?: "Failed to get power data"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting power data", e)
            Result.failure(e)
        }
    }
    
    /**
     * 扫描发现新设备
     */
    suspend fun scanForDevices(): Result<List<Device>> {
        return try {
            val response = apiService.scanDevices()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Log.d(TAG, "Discovered ${apiResponse.data.size} devices")
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception((apiResponse as? com.sslab.hmi.data.model.ApiResponse<*>)?.error ?: "Failed to scan devices"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning devices", e)
            Result.failure(e)
        }
    }
    
    /**
     * 订阅设备状态更新
     */
    fun subscribeToDeviceUpdates(deviceId: String) {
        webSocketService.subscribeToDevice(deviceId)
    }
    
    /**
     * 取消订阅设备状态更新
     */
    fun unsubscribeFromDeviceUpdates(deviceId: String) {
        webSocketService.unsubscribeFromDevice(deviceId)
    }
    
    /**
     * 获取设备状态更新流
     */
    fun getDeviceStatusUpdates(): Flow<DeviceStatusUpdate> {
        return webSocketService.deviceStatusUpdates
    }
    
    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * 监听WebSocket连接状态
     */
    private fun observeWebSocketStatus() {
        // 这里可以使用coroutine scope来收集WebSocket状态
        // 为了简化，先使用简单的状态管理
    }
    
    /**
     * 监听设备状态更新
     */
    private fun observeDeviceStatusUpdates() {
        // 监听设备状态更新并更新本地设备列表
        // 这里可以根据收到的状态更新来更新对应设备的状态
    }
}
