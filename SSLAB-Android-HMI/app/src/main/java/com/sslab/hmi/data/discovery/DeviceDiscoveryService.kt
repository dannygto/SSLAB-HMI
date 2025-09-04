package com.sslab.hmi.data.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.sslab.hmi.data.model.Device
import com.sslab.hmi.data.model.DeviceType
import com.sslab.hmi.data.model.DeviceStatus
import com.sslab.hmi.data.model.GroupDeviceStats
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设备发现服务（mDNS/Bonjour）
 */
@Singleton
class DeviceDiscoveryService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "DeviceDiscoveryService"
        private const val SERVICE_TYPE = "_sslab._tcp."
        private const val GROUP_ID_KEY = "groupId"
    }
    
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var isDiscovering = false
    
    // 当前教室分组ID
    private val _currentGroupId = MutableStateFlow("")
    val currentGroupId: StateFlow<String> = _currentGroupId.asStateFlow()
    
    // 发现的设备列表
    private val discoveredDevices = mutableMapOf<String, Device>()
    
    // 设备发现结果流
    private val _deviceDiscovered = MutableSharedFlow<Device>()
    val deviceDiscovered: SharedFlow<Device> = _deviceDiscovered.asSharedFlow()
    
    // 设备丢失流
    private val _deviceLost = MutableSharedFlow<String>()
    val deviceLost: SharedFlow<String> = _deviceLost.asSharedFlow()
    
    // 发现状态流
    private val _discoveryState = MutableSharedFlow<DiscoveryState>()
    val discoveryState: SharedFlow<DiscoveryState> = _discoveryState.asSharedFlow()
    
    // 分组设备统计流
    private val _groupStats = MutableStateFlow(GroupDeviceStats(""))
    val groupStats: StateFlow<GroupDeviceStats> = _groupStats.asStateFlow()
    
    /**
     * 设置当前教室分组ID
     */
    fun setGroupId(groupId: String) {
        _currentGroupId.value = groupId
        Log.d(TAG, "Set group ID to: $groupId")
        updateGroupStats()
    }
    
    /**
     * 开始设备发现
     */
    fun startDiscovery(groupId: String? = null) {
        groupId?.let { setGroupId(it) }
        
        if (isDiscovering) {
            Log.d(TAG, "Discovery already running")
            return
        }
        
        if (_currentGroupId.value.isEmpty()) {
            Log.w(TAG, "Group ID not set, cannot start discovery")
            _discoveryState.tryEmit(DiscoveryState.ERROR)
            return
        }
        
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: Error code: $errorCode")
                _discoveryState.tryEmit(DiscoveryState.ERROR)
            }
            
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: Error code: $errorCode")
            }
            
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d(TAG, "Discovery started")
                isDiscovering = true
                _discoveryState.tryEmit(DiscoveryState.DISCOVERING)
            }
            
            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped")
                isDiscovering = false
                _discoveryState.tryEmit(DiscoveryState.STOPPED)
            }
            
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
                resolveService(serviceInfo)
            }
            
            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
                val deviceId = serviceInfo.serviceName
                discoveredDevices.remove(deviceId)
                _deviceLost.tryEmit(deviceId)
            }
        }
        
        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start discovery", e)
            _discoveryState.tryEmit(DiscoveryState.ERROR)
        }
    }
    
    /**
     * 停止设备发现
     */
    fun stopDiscovery() {
        if (!isDiscovering) {
            Log.d(TAG, "Discovery not running")
            return
        }
        
        try {
            discoveryListener?.let { listener ->
                nsdManager.stopServiceDiscovery(listener)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop discovery", e)
        }
    }
    
    /**
     * 解析服务信息
     */
    private fun resolveService(serviceInfo: NsdServiceInfo) {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed: ${serviceInfo.serviceName}, Error code: $errorCode")
            }
            
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service resolved: ${serviceInfo.serviceName}")
                
                // 检查设备是否属于当前分组
                val device = createDeviceFromServiceInfo(serviceInfo)
                if (device.groupId == _currentGroupId.value || _currentGroupId.value.isEmpty()) {
                    discoveredDevices[device.id] = device
                    _deviceDiscovered.tryEmit(device)
                    updateGroupStats()
                } else {
                    Log.d(TAG, "Device ${device.id} belongs to group ${device.groupId}, ignoring")
                }
            }
        }
        
        try {
            nsdManager.resolveService(serviceInfo, resolveListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve service", e)
        }
    }
    
    /**
     * 从服务信息创建设备对象
     */
    private fun createDeviceFromServiceInfo(serviceInfo: NsdServiceInfo): Device {
        val serviceName = serviceInfo.serviceName
        val host = serviceInfo.host?.hostAddress ?: "unknown"
        val port = serviceInfo.port
        
        // 从TXT记录中获取分组ID，暂时返回空字符串
        val groupId = ""  // TODO: 实现TXT记录解析
        
        // 从服务名称解析设备类型
        val deviceType = when {
            serviceName.contains("power", ignoreCase = true) -> DeviceType.TEACHING_POWER.apiValue
            serviceName.contains("environment", ignoreCase = true) -> DeviceType.ENVIRONMENT_MONITOR.apiValue
            serviceName.contains("lift", ignoreCase = true) -> DeviceType.LIFT_CONTROL.apiValue
            serviceName.contains("control", ignoreCase = true) -> DeviceType.DEVICE_CONTROL.apiValue
            else -> DeviceType.UNKNOWN.apiValue
        }
        
        return Device(
            id = serviceName,
            name = serviceName,
            type = deviceType,
            ipAddress = host,
            port = port,
            status = DeviceStatus.ONLINE.name,
            groupId = groupId,
            lastUpdateTime = System.currentTimeMillis()
        )
    }
    
    /**
     * 获取已发现的设备列表（仅当前分组）
     */
    fun getDiscoveredDevices(): List<Device> {
        return discoveredDevices.values.filter { 
            it.groupId == _currentGroupId.value || _currentGroupId.value.isEmpty()
        }
    }
    
    /**
     * 获取指定分组的设备列表
     */
    fun getDevicesByGroup(groupId: String): List<Device> {
        return discoveredDevices.values.filter { it.groupId == groupId }
    }
    
    /**
     * 更新分组统计信息
     */
    private fun updateGroupStats() {
        val currentGroup = _currentGroupId.value
        val groupDevices = getDevicesByGroup(currentGroup)
        
        _groupStats.value = GroupDeviceStats(
            groupId = currentGroup,
            totalDevices = groupDevices.size,
            onlineDevices = groupDevices.count { it.status == DeviceStatus.ONLINE.name },
            offlineDevices = groupDevices.count { it.status == DeviceStatus.OFFLINE.name },
            errorDevices = groupDevices.count { it.status == DeviceStatus.ERROR.name },
            lastScanTime = System.currentTimeMillis()
        )
    }
    
    /**
     * 手动添加设备（用于模拟器）
     */
    fun addSimulatedDevice(device: Device) {
        discoveredDevices[device.id] = device
        _deviceDiscovered.tryEmit(device)
        updateGroupStats()
    }
    
    /**
     * 初始化模拟设备（开发阶段使用）
     */
    fun initializeSimulatedDevices(groupId: String = "HX001") {
        setGroupId(groupId)
        
        val simulatedDevices = listOf(
            Device(
                id = "teaching-power-${groupId}-01",
                name = "教学电源控制器01",
                type = DeviceType.TEACHING_POWER.apiValue,
                ipAddress = "192.168.1.101",
                port = 80,
                status = DeviceStatus.ONLINE.name,
                location = "实验室${groupId}",
                groupId = groupId
            ),
            Device(
                id = "environment-monitor-${groupId}-01",
                name = "环境监测器01",
                type = DeviceType.ENVIRONMENT_MONITOR.apiValue,
                ipAddress = "192.168.1.102",
                port = 80,
                status = DeviceStatus.ONLINE.name,
                location = "实验室${groupId}",
                groupId = groupId
            ),
            Device(
                id = "lift-control-${groupId}-01",
                name = "升降台控制器01",
                type = DeviceType.LIFT_CONTROL.apiValue,
                ipAddress = "192.168.1.103",
                port = 80,
                status = DeviceStatus.ONLINE.name,
                location = "实验室${groupId}",
                groupId = groupId
            ),
            Device(
                id = "device-control-${groupId}-01",
                name = "设备控制器01",
                type = DeviceType.DEVICE_CONTROL.apiValue,
                ipAddress = "192.168.1.104",
                port = 80,
                status = DeviceStatus.ONLINE.name,
                location = "实验室${groupId}",
                groupId = groupId
            )
        )
        
        simulatedDevices.forEach { device ->
            addSimulatedDevice(device)
        }
        
        Log.d(TAG, "Initialized ${simulatedDevices.size} simulated devices for group $groupId")
    }
}

/**
 * 设备发现状态枚举
 */
enum class DiscoveryState {
    IDLE,
    DISCOVERING,
    STOPPED,
    ERROR
}
