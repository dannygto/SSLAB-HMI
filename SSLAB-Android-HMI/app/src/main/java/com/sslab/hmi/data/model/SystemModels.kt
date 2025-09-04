package com.sslab.hmi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 环境监测数据模型
 */
@Entity(tableName = "environment_data")
data class EnvironmentData(
    @PrimaryKey
    val deviceId: String,
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    val airQuality: Float = 0f,
    val lightIntensity: Float = 0f,
    val noiseLevel: Float = 0f,
    val co2Level: Float = 0f,
    val tvocLevel: Float = 0f,
    val pm25: Float = 0f,
    val pm10: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 升降台控制数据模型
 */
@Entity(tableName = "lift_control")
data class LiftControl(
    @PrimaryKey
    val deviceId: String,
    val position: Float = 0f, // 当前位置 (0-100%)
    val targetPosition: Float = 0f, // 目标位置
    val isMoving: Boolean = false,
    val speed: Int = 50, // 速度 (1-100)
    val direction: LiftDirection = LiftDirection.STOP,
    val weight: Float = 0f, // 当前负载重量
    val maxWeight: Float = 100f, // 最大承重
    val safetyLock: Boolean = false,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 升降台方向枚举
 */
enum class LiftDirection(val displayName: String) {
    @SerializedName("up")
    UP("上升"),
    
    @SerializedName("down")
    DOWN("下降"),
    
    @SerializedName("stop")
    STOP("停止")
}

/**
 * 设备控制数据模型
 */
@Entity(tableName = "device_control")
data class DeviceControl(
    @PrimaryKey
    val deviceId: String,
    val deviceName: String = "",
    val controlType: ControlType = ControlType.SWITCH,
    val value: String = "",
    val enabled: Boolean = false,
    val locked: Boolean = false,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 控制类型枚举
 */
enum class ControlType(val displayName: String) {
    @SerializedName("switch")
    SWITCH("开关"),
    
    @SerializedName("dimmer")
    DIMMER("调光"),
    
    @SerializedName("motor")
    MOTOR("电机"),
    
    @SerializedName("valve")
    VALVE("阀门"),
    
    @SerializedName("sensor")
    SENSOR("传感器")
}

/**
 * 通用控制命令
 */
data class ControlCommand(
    val deviceId: String,
    val command: String,
    val parameters: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)
