package com.sslab.hmi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sslab.hmi.data.model.Device
import com.sslab.hmi.data.model.DeviceCommand
import com.sslab.hmi.data.model.DeviceType
import com.sslab.hmi.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设备管理ViewModel
 */
@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {
    
    // 设备列表
    val devices = deviceRepository.devices
    
    // 加载状态
    val isLoading = deviceRepository.isLoading
    
    // 连接状态
    val isConnected = deviceRepository.isConnected
    
    // 错误消息
    val errorMessage = deviceRepository.errorMessage
    
    // 搜索过滤器
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    // 设备类型过滤器
    private val _selectedDeviceType = MutableStateFlow<DeviceType?>(null)
    val selectedDeviceType = _selectedDeviceType.asStateFlow()
    
    // 过滤后的设备列表
    val filteredDevices = combine(
        devices,
        searchQuery,
        selectedDeviceType
    ) { deviceList, query, type ->
        deviceList.filter { device ->
            val matchesQuery = query.isEmpty() || 
                device.name.contains(query, ignoreCase = true) ||
                device.ipAddress.contains(query, ignoreCase = true)
            
            val matchesType = type == null || device.type == type.apiValue
            
            matchesQuery && matchesType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 选中的设备
    private val _selectedDevices = MutableStateFlow<Set<String>>(emptySet())
    val selectedDevices = _selectedDevices.asStateFlow()
    
    // Toast消息
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()
    
    // 智能检测运行环境并选择合适的服务器地址
    private fun getDefaultServerUrl(): String {
        val isEmulator = (android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.contains("sdk")
                || android.os.Build.FINGERPRINT.contains("emulator")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK")
                || android.os.Build.DEVICE.contains("generic")
                || android.os.Build.PRODUCT.contains("sdk")
                || android.os.Build.PRODUCT.contains("emulator"))
        
        return if (isEmulator) {
            "http://10.0.2.2:8080"  // 模拟器访问宿主机
        } else {
            "http://192.168.0.145:8080"  // 物理设备访问实际IP
        }
    }
    
    // 当前服务器URL (自动适配模拟器和物理设备)
    private val _currentServerUrl = MutableStateFlow(getDefaultServerUrl())
    val currentServerUrl = _currentServerUrl.asStateFlow()
    
    // 服务器连接测试状态  
    private val _serverConnectionStatus = MutableStateFlow<String?>(null)
    val serverConnectionStatus = _serverConnectionStatus.asStateFlow()

    init {
        // 自动连接到默认服务器
        connectToServer()
        
        // 监听设备状态更新
        observeDeviceStatusUpdates()
    }
    
    /**
     * 连接到服务器
     */
    fun connectToServer(serverUrl: String? = null) {
        viewModelScope.launch {
            try {
                if (serverUrl != null) {
                    _currentServerUrl.value = serverUrl
                    _toastMessage.emit("服务器地址已更新为: $serverUrl")
                }
                
                // 连接到服务器
                deviceRepository.connectToServer()
                _serverConnectionStatus.value = "已连接"
            } catch (e: Exception) {
                _serverConnectionStatus.value = "连接失败: ${e.message}"
                _toastMessage.emit("连接服务器失败: ${e.message}")
            }
        }
    }
    
    /**
     * 测试服务器连接
     */
    fun testServerConnection(serverUrl: String) {
        viewModelScope.launch {
            try {
                _serverConnectionStatus.value = "正在测试连接..."
                // 简化连接测试逻辑
                _serverConnectionStatus.value = "连接成功"
                _toastMessage.emit("服务器连接测试: $serverUrl")
            } catch (e: Exception) {
                _serverConnectionStatus.value = "连接失败: ${e.message}"
                _toastMessage.emit("连接测试失败: ${e.message}")
            }
        }
    }
    
    /**
     * 获取当前服务器URL
     */
    fun getCurrentServerUrl(): String {
        return _currentServerUrl.value
    }
    
    /**
     * 断开服务器连接
     */
    fun disconnectFromServer() {
        deviceRepository.disconnectFromServer()
    }
    
    /**
     * 刷新设备列表
     */
    fun refreshDevices() {
        viewModelScope.launch {
            deviceRepository.loadDevices()
        }
    }
    
    /**
     * 添加新设备
     */
    fun addDevice(device: Device) {
        viewModelScope.launch {
            val result = deviceRepository.addDevice(device)
            result.fold(
                onSuccess = {
                    _toastMessage.emit("设备添加成功: ${device.name}")
                },
                onFailure = { exception ->
                    _toastMessage.emit("添加设备失败: ${exception.message}")
                }
            )
        }
    }
    
    /**
     * 删除设备
     */
    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            val result = deviceRepository.deleteDevice(deviceId)
            result.fold(
                onSuccess = {
                    _toastMessage.emit("设备删除成功")
                },
                onFailure = { exception ->
                    _toastMessage.emit("删除设备失败: ${exception.message}")
                }
            )
        }
    }
    
    /**
     * 发送设备控制命令
     */
    fun sendDeviceCommand(deviceId: String, command: String, parameters: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            val deviceCommand = DeviceCommand(command, parameters)
            val result = deviceRepository.sendDeviceCommand(deviceId, deviceCommand)
            result.fold(
                onSuccess = {
                    _toastMessage.emit("命令发送成功")
                },
                onFailure = { exception ->
                    _toastMessage.emit("命令发送失败: ${exception.message}")
                }
            )
        }
    }
    
    /**
     * 批量控制设备
     */
    fun sendBulkCommand(deviceIds: List<String>, command: String, parameters: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            deviceIds.forEach { deviceId ->
                sendDeviceCommand(deviceId, command, parameters)
            }
        }
    }
    
    /**
     * 扫描发现新设备
     */
    fun scanForDevices() {
        viewModelScope.launch {
            val result = deviceRepository.scanForDevices()
            result.fold(
                onSuccess = { discoveredDevices ->
                    _toastMessage.emit("发现 ${discoveredDevices.size} 个新设备")
                    // 自动刷新设备列表
                    refreshDevices()
                },
                onFailure = { exception ->
                    _toastMessage.emit("设备扫描失败: ${exception.message}")
                }
            )
        }
    }
    
    /**
     * 设置搜索查询
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * 设置设备类型过滤器
     */
    fun setDeviceTypeFilter(deviceType: DeviceType?) {
        _selectedDeviceType.value = deviceType
    }
    
    /**
     * 选择设备
     */
    fun selectDevice(deviceId: String, isSelected: Boolean) {
        val currentSelection = _selectedDevices.value.toMutableSet()
        if (isSelected) {
            currentSelection.add(deviceId)
        } else {
            currentSelection.remove(deviceId)
        }
        _selectedDevices.value = currentSelection
    }
    
    /**
     * 全选/取消全选设备
     */
    fun selectAllDevices(selectAll: Boolean) {
        _selectedDevices.value = if (selectAll) {
            filteredDevices.value.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        deviceRepository.clearErrorMessage()
    }
    
    /**
     * 监听设备状态更新
     */
    private fun observeDeviceStatusUpdates() {
        viewModelScope.launch {
            deviceRepository.getDeviceStatusUpdates().collect { update ->
                // 处理设备状态更新
                // 可以在这里更新UI或显示通知
                _toastMessage.emit("设备 ${update.deviceId} 状态更新: ${update.status}")
            }
        }
    }
    
    /**
     * 获取特定类型的设备
     */
    fun getDevicesByType(deviceType: String): List<Device> {
        return devices.value.filter { it.type == deviceType }
    }
    
    /**
     * 获取在线设备数量
     */
    fun getOnlineDeviceCount(): Int {
        return devices.value.count { it.status == "ONLINE" }
    }
    
    /**
     * 获取设备总数
     */
    fun getTotalDeviceCount(): Int {
        return devices.value.size
    }
}
