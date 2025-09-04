package com.sslab.hmi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 设备基础数据模型 - 与Web平台完全对齐
 */
@Entity(tableName = "devices")
data class Device(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: DeviceType,
    val ipAddress: String,
    val port: Int = 80,
    val status: DeviceStatus = DeviceStatus.OFFLINE,
    val location: String = "",
    val groupId: String = "", // 设备分组标识，如HX001
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val enabled: Boolean = true,
    val model: String = "",
    val data: Map<String, Any> = emptyMap()
)

/**
 * 设备类型枚举 - 与Web平台SSLAB设备类型完全对齐
 */
enum class DeviceType(val displayName: String, val apiValue: String) {
    @SerializedName("ENVIRONMENT_MONITOR")
    ENVIRONMENT_MONITOR("环境监测装置", "ENVIRONMENT_MONITOR"),
    
    @SerializedName("STUDENT_POWER_TERMINAL")
    STUDENT_POWER_TERMINAL("学生电源终端", "STUDENT_POWER_TERMINAL"),
    
    @SerializedName("ENVIRONMENT_CONTROLLER")
    ENVIRONMENT_CONTROLLER("环境控制装置", "ENVIRONMENT_CONTROLLER"),
    
    @SerializedName("CURTAIN_CONTROLLER")
    CURTAIN_CONTROLLER("窗帘控制装置", "CURTAIN_CONTROLLER"),
    
    @SerializedName("LIGHTING_CONTROLLER")
    LIGHTING_CONTROLLER("灯光控制装置", "LIGHTING_CONTROLLER"),
    
    @SerializedName("LIFT_CONTROLLER")
    LIFT_CONTROLLER("升降控制装置", "LIFT_CONTROLLER"),
    
    @SerializedName("UNKNOWN")
    UNKNOWN("未知设备", "UNKNOWN")
}

/**
 * 设备状态枚举
 */
enum class DeviceStatus(val displayName: String) {
    @SerializedName("ONLINE")
    ONLINE("在线"),
    
    @SerializedName("OFFLINE")
    OFFLINE("离线"),
    
    @SerializedName("ERROR")
    ERROR("错误"),
    
    @SerializedName("MAINTENANCE")
    MAINTENANCE("维护中")
}

/**
 * API响应通用格式
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: String?
)

/**
 * 添加设备请求
 */
data class AddDeviceRequest(
    val name: String,
    val type: String,
    val groupId: String? = null,
    val ipAddress: String? = null,
    val port: Int = 80
)

/**
 * 更新设备请求
 */
data class UpdateDeviceRequest(
    val name: String? = null,
    val groupId: String? = null,
    val location: String? = null,
    val enabled: Boolean? = null
)

/**
 * 设备控制命令
 */
data class DeviceControlCommand(
    val command: String,
    val params: Map<String, Any> = emptyMap()
)

/**
 * 批量控制请求
 */
data class BatchControlRequest(
    val deviceIds: List<String>,
    val command: String,
    val params: Map<String, Any> = emptyMap()
)

/**
 * 设备搜索请求
 */
data class DeviceSearchRequest(
    val query: String? = null,
    val type: String? = null,
    val groupId: String? = null,
    val enabled: Boolean? = null
)
    
    @SerializedName("offline")
    OFFLINE("离线"),
    
    @SerializedName("error")
    ERROR("故障"),
    
    @SerializedName("maintenance")
    MAINTENANCE("维护中")
}
