package com.sslab.hmi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 设备基础数据模型
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
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 设备类型枚举
 */
enum class DeviceType(val displayName: String) {
    @SerializedName("teaching_power")
    TEACHING_POWER("教学电源"),
    
    @SerializedName("environment_monitor")
    ENVIRONMENT_MONITOR("环境监测"),
    
    @SerializedName("lift_control")
    LIFT_CONTROL("升降台控制"),
    
    @SerializedName("device_control")
    DEVICE_CONTROL("设备控制"),
    
    @SerializedName("unknown")
    UNKNOWN("未知设备")
}

/**
 * 设备状态枚举
 */
enum class DeviceStatus(val displayName: String) {
    @SerializedName("online")
    ONLINE("在线"),
    
    @SerializedName("offline")
    OFFLINE("离线"),
    
    @SerializedName("error")
    ERROR("故障"),
    
    @SerializedName("maintenance")
    MAINTENANCE("维护中")
}
