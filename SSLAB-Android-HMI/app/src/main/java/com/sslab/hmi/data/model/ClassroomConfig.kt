package com.sslab.hmi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 教室配置数据模型
 */
@Entity(tableName = "classroom_config")
data class ClassroomConfig(
    @PrimaryKey
    val id: String = "default",
    val classroomName: String = "",
    val groupId: String = "", // 教室分组标识，如HX001
    val buildingName: String = "",
    val floorNumber: Int = 1,
    val roomNumber: String = "",
    val teacherName: String = "",
    val isActive: Boolean = true,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 设备发现配置
 */
data class DeviceDiscoveryConfig(
    val groupId: String,
    val enableAutoDiscovery: Boolean = true,
    val discoveryTimeout: Long = 30000, // 30秒
    val serviceType: String = "_sslab._tcp.",
    val broadcastPort: Int = 8888
)

/**
 * 分组设备统计
 */
data class GroupDeviceStats(
    val groupId: String,
    val totalDevices: Int = 0,
    val onlineDevices: Int = 0,
    val offlineDevices: Int = 0,
    val errorDevices: Int = 0,
    val lastScanTime: Long = System.currentTimeMillis()
)
