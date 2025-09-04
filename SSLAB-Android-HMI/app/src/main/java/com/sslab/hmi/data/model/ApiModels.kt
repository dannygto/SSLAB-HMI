package com.sslab.hmi.data.model

import com.google.gson.annotations.SerializedName

/**
 * API数据模型 - 与Web平台API完全对齐
 */

// ========== 分组相关模型 ==========

/**
 * 设备分组
 */
data class DeviceGroup(
    val id: String,
    val name: String,
    val description: String = "",
    val deviceIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 创建分组请求
 */
data class CreateGroupRequest(
    val name: String,
    val description: String = ""
)

/**
 * 更新分组请求
 */
data class UpdateGroupRequest(
    val name: String? = null,
    val description: String? = null
)

// ========== 统计信息模型 ==========

/**
 * 系统统计信息
 */
data class SystemStats(
    val deviceCount: Int,
    val onlineDevices: Int,
    val offlineDevices: Int,
    val groupCount: Int,
    val connectedClients: Int,
    val systemUptime: String,
    val memoryUsage: MemoryUsage
)

/**
 * 设备统计信息
 */
data class DeviceStats(
    val totalDevices: Int,
    val devicesByType: Map<String, Int>,
    val devicesByStatus: Map<String, Int>,
    val devicesByGroup: Map<String, Int>
)

/**
 * 分组统计信息
 */
data class GroupStats(
    val totalGroups: Int,
    val devicesPerGroup: Map<String, Int>,
    val averageDevicesPerGroup: Double
)

/**
 * 内存使用情况
 */
data class MemoryUsage(
    val total: Long,
    val free: Long,
    val used: Long,
    val usagePercent: Double
)

// ========== 系统健康状态模型 ==========

/**
 * 系统健康状态
 */
data class SystemHealth(
    val status: String,
    val timestamp: Long,
    val server: ServerHealth,
    val system: SystemInfo,
    val application: ApplicationInfo
)

/**
 * 服务器健康状态
 */
data class ServerHealth(
    val status: String,
    val uptime: Long,
    val uptimeFormatted: String,
    val pid: Int
)

/**
 * 系统信息
 */
data class SystemInfo(
    val platform: String,
    val nodeVersion: String,
    val cpuUsage: Double,
    val memory: MemoryInfo
)

/**
 * 内存信息
 */
data class MemoryInfo(
    val total: Long,
    val free: Long,
    val used: Long,
    val usagePercent: Double
)

/**
 * 应用信息
 */
data class ApplicationInfo(
    val deviceCount: Int,
    val groupCount: Int,
    val connectedClients: Int,
    val apiCallsToday: Int
)

// ========== 设备类型信息模型 ==========

/**
 * 设备类型信息
 */
data class DeviceTypeInfo(
    val type: String,
    val name: String,
    val description: String,
    val model: String
)

/**
 * 设备能力信息
 */
data class DeviceCapabilities(
    val type: String,
    val name: String,
    val model: String,
    val capabilities: List<String>,
    val commands: List<String>,
    val dataFormat: Map<String, Any>
)

/**
 * API文档
 */
data class ApiDocumentation(
    val title: String,
    val version: String,
    val description: String,
    val baseUrl: String,
    val endpoints: Map<String, Any>
)

// ========== 测试设备创建模型 ==========

/**
 * 创建测试设备请求
 */
data class CreateTestDevicesRequest(
    val count: Int = 100,
    val createGroups: Boolean = true,
    val groupCount: Int = 4
)

// ========== 设备发现模型 ==========

/**
 * 设备发现结果
 */
data class DeviceDiscoveryResult(
    val discoveredDevices: List<DiscoveredDevice>,
    val scanDuration: Long,
    val scanNetwork: String
)

/**
 * 发现的设备
 */
data class DiscoveredDevice(
    val ip: String,
    val mac: String,
    val type: String,
    val name: String,
    val signal: Int
)

// ========== 控制结果模型 ==========

/**
 * 分组控制结果
 */
data class GroupControlResult(
    val groupId: String,
    val successCount: Int,
    val failureCount: Int,
    val results: List<DeviceControlResult>
)

/**
 * 设备控制结果
 */
data class DeviceControlResult(
    val deviceId: String,
    val success: Boolean,
    val message: String,
    val response: Map<String, Any>? = null
)

// ========== SSLAB设备数据模型 ==========

/**
 * 环境监测装置数据
 */
data class EnvironmentMonitorData(
    val temperature: Double,
    val humidity: Double,
    val co2: Double,
    val lightIntensity: Double,
    val airQuality: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 学生电源终端数据
 */
data class StudentPowerTerminalData(
    val voltage: Double,
    val current: Double,
    val power: Double,
    val energy: Double,
    val state: String, // ON|OFF
    val safety: PowerSafetyData,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 电源安全数据
 */
data class PowerSafetyData(
    val overCurrent: Boolean,
    val overVoltage: Boolean,
    val shortCircuit: Boolean
)

/**
 * 环境控制装置数据
 */
data class EnvironmentControllerData(
    val water: WaterSystemData,
    val ventilation: VentilationSystemData,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 供水系统数据
 */
data class WaterSystemData(
    val state: String, // ON|OFF
    val flow: Double,
    val pressure: Double
)

/**
 * 通风系统数据
 */
data class VentilationSystemData(
    val state: String, // ON|OFF
    val speed: Double,
    val direction: String // IN|OUT
)

/**
 * 窗帘控制装置数据
 */
data class CurtainControllerData(
    val state: String, // OPEN|CLOSE|MOVING
    val position: Double, // 0-100%
    val target: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 灯光控制装置数据
 */
data class LightingControllerData(
    val state: String, // ON|OFF
    val brightness: Double, // 0-100%
    val colorTemp: Double, // 2700-6500K
    val power: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 升降控制装置数据
 */
data class LiftControllerData(
    val state: String, // UP|DOWN|STOP
    val position: Double, // 0-100%
    val target: Double,
    val load: Double,
    val timestamp: Long = System.currentTimeMillis()
)
