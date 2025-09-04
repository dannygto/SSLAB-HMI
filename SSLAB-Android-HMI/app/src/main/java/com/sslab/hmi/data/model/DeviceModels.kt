package com.sslab.hmi.data.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * SSLAB设备类型枚举
 */
enum class DeviceType(val displayName: String) {
    @SerializedName("ENVIRONMENT_MONITOR")
    ENVIRONMENT_MONITOR("环境监测设备"),
    
    @SerializedName("STUDENT_POWER_TERMINAL")
    STUDENT_POWER_TERMINAL("学生电源终端"),
    
    @SerializedName("ENVIRONMENT_CONTROLLER")
    ENVIRONMENT_CONTROLLER("环境控制器"),
    
    @SerializedName("CURTAIN_CONTROLLER")
    CURTAIN_CONTROLLER("窗帘控制器"),
    
    @SerializedName("LIGHTING_CONTROLLER")
    LIGHTING_CONTROLLER("照明控制器"),
    
    @SerializedName("LIFT_CONTROLLER")
    LIFT_CONTROLLER("升降控制器")
}

/**
 * 设备基础信息
 */
data class Device(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("type")
    val type: DeviceType,
    
    @SerializedName("ipAddress")
    val ipAddress: String,
    
    @SerializedName("port")
    val port: Int,
    
    @SerializedName("macAddress")
    val macAddress: String,
    
    @SerializedName("firmwareVersion")
    val firmwareVersion: String,
    
    @SerializedName("lastSeen")
    val lastSeen: Date,
    
    @SerializedName("isOnline")
    val isOnline: Boolean,
    
    @SerializedName("attributes")
    val attributes: Map<String, Any> = emptyMap()
)

/**
 * 环境监测设备数据
 */
data class EnvironmentData(
    @SerializedName("temperature")
    val temperature: Float,
    
    @SerializedName("humidity")
    val humidity: Float,
    
    @SerializedName("airQuality")
    val airQuality: Int,
    
    @SerializedName("lightLevel")
    val lightLevel: Int,
    
    @SerializedName("timestamp")
    val timestamp: Date
)

/**
 * 电源控制数据
 */
data class PowerControlData(
    @SerializedName("voltage")
    val voltage: Float,
    
    @SerializedName("current")
    val current: Float,
    
    @SerializedName("power")
    val power: Float,
    
    @SerializedName("isEnabled")
    val isEnabled: Boolean,
    
    @SerializedName("timestamp")
    val timestamp: Date
)

/**
 * 设备控制命令
 */
data class DeviceCommand(
    @SerializedName("command")
    val command: String,
    
    @SerializedName("parameters")
    val parameters: Map<String, Any> = emptyMap()
)

/**
 * API响应基础类
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: String? = null
)

/**
 * 设备发现结果
 */
data class DeviceDiscoveryResult(
    @SerializedName("devices")
    val devices: List<Device>
)

/**
 * 设备状态更新
 */
data class DeviceStatusUpdate(
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("data")
    val data: Map<String, Any>,
    
    @SerializedName("timestamp")
    val timestamp: Date
)

/**
 * 设备能力信息
 */
data class DeviceCapabilities(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("displayName")
    val displayName: String,
    
    @SerializedName("commands")
    val commands: List<String>,
    
    @SerializedName("attributes")
    val attributes: List<String>
)

/**
 * 系统统计信息
 */
data class SystemStats(
    @SerializedName("uptime")
    val uptime: Long,
    
    @SerializedName("deviceCount")
    val deviceCount: Int,
    
    @SerializedName("onlineDevices")
    val onlineDevices: Int,
    
    @SerializedName("groupCount")
    val groupCount: Int,
    
    @SerializedName("apiCalls")
    val apiCalls: Long,
    
    @SerializedName("memoryUsage")
    val memoryUsage: Map<String, Any>
)

/**
 * 设备分组
 */
data class DeviceGroup(
    @SerializedName("groupId")
    val groupId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("devices")
    val devices: List<Device> = emptyList()
)

/**
 * 创建分组请求
 */
data class CreateGroupRequest(
    @SerializedName("groupId")
    val groupId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null
)

/**
 * 分组控制结果
 */
data class GroupControlResult(
    @SerializedName("affectedDevices")
    val affectedDevices: Int,
    
    @SerializedName("results")
    val results: List<Map<String, Any>>
)
