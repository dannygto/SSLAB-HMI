package com.sslab.hmi.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 教学电源数据模型
 */
@Entity(tableName = "teaching_power")
data class TeachingPower(
    @PrimaryKey
    val deviceId: String,
    val lowVoltageEnabled: Boolean = false,
    val highVoltageEnabled: Boolean = false,
    val acVoltage: Float = 0f,
    val dcVoltage: Float = 0f,
    val current: Float = 0f,
    val temperature: Float = 0f,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 学生电源组数据模型
 */
@Entity(tableName = "student_power_groups")
data class StudentPowerGroup(
    @PrimaryKey
    val groupId: String,
    val groupName: String,
    val deviceCount: Int = 0,
    val enabledCount: Int = 0,
    val totalPower: Float = 0f,
    val avgVoltage: Float = 0f,
    val avgCurrent: Float = 0f,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 学生设备数据模型
 */
@Entity(tableName = "student_devices")
data class StudentDevice(
    @PrimaryKey
    val deviceId: String,
    val groupId: String,
    val studentName: String = "",
    val seatNumber: String = "",
    val enabled: Boolean = false,
    val voltage: Float = 0f,
    val current: Float = 0f,
    val power: Float = 0f,
    val status: StudentDeviceStatus = StudentDeviceStatus.NORMAL,
    val lastUpdateTime: Long = System.currentTimeMillis()
)

/**
 * 学生设备状态枚举
 */
enum class StudentDeviceStatus(val displayName: String) {
    @SerializedName("normal")
    NORMAL("正常"),
    
    @SerializedName("overload")
    OVERLOAD("过载"),
    
    @SerializedName("short_circuit")
    SHORT_CIRCUIT("短路"),
    
    @SerializedName("disconnected")
    DISCONNECTED("断开")
}

/**
 * 电源控制命令
 */
data class PowerControlCommand(
    val deviceId: String,
    val command: PowerCommand,
    val value: Any? = null
)

/**
 * 电源控制命令类型
 */
enum class PowerCommand {
    @SerializedName("enable_low_voltage")
    ENABLE_LOW_VOLTAGE,
    
    @SerializedName("disable_low_voltage")
    DISABLE_LOW_VOLTAGE,
    
    @SerializedName("enable_high_voltage")
    ENABLE_HIGH_VOLTAGE,
    
    @SerializedName("disable_high_voltage")
    DISABLE_HIGH_VOLTAGE,
    
    @SerializedName("set_voltage")
    SET_VOLTAGE,
    
    @SerializedName("emergency_stop")
    EMERGENCY_STOP,
    
    @SerializedName("reset")
    RESET
}
