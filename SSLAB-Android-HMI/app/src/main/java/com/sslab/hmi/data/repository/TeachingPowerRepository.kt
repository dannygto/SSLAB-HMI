package com.sslab.hmi.data.repository

import android.util.Log
import com.sslab.hmi.data.api.TeachingPowerApi
import com.sslab.hmi.data.database.TeachingPowerDao
import com.sslab.hmi.data.database.StudentPowerGroupDao
import com.sslab.hmi.data.database.StudentDeviceDao
import com.sslab.hmi.data.model.*
import com.sslab.hmi.data.websocket.WebSocketService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 教学电源数据仓库
 */
@Singleton
class TeachingPowerRepository @Inject constructor(
    private val teachingPowerApi: TeachingPowerApi,
    private val teachingPowerDao: TeachingPowerDao,
    private val studentPowerGroupDao: StudentPowerGroupDao,
    private val studentDeviceDao: StudentDeviceDao,
    private val webSocketService: WebSocketService,
    private val deviceRepository: DeviceRepository
) {
    
    companion object {
        private const val TAG = "TeachingPowerRepository"
    }
    
    /**
     * 获取教学电源状态
     */
    fun getTeachingPowerStatus(deviceId: String): Flow<TeachingPower?> {
        return teachingPowerDao.getTeachingPowerByDevice(deviceId)
    }
    
    /**
     * 获取所有设备列表
     */
    suspend fun getDevices(): List<Device> {
        return deviceRepository.getAllDevices()
    }
    
    /**
     * 获取学生电源组列表
     */
    suspend fun getStudentGroups(): List<StudentGroup> {
        // 将StudentPowerGroup转换为StudentGroup
        val powerGroups = studentPowerGroupDao.getAllGroupsSync()
        return powerGroups.map { powerGroup ->
            StudentGroup(
                id = powerGroup.groupId,
                name = powerGroup.groupName,
                deviceIds = emptyList(), // StudentPowerGroup没有deviceIds，使用空列表
                isPowerOn = powerGroup.enabledCount > 0
            )
        }
    }
    
    /**
     * 获取学生电源组列表（Flow版本）
     */
    fun getStudentGroupsFlow(): Flow<List<StudentPowerGroup>> {
        return studentPowerGroupDao.getAllGroups()
    }
    
    /**
     * 获取学生设备列表
     */
    fun getStudentDevices(groupId: String): Flow<List<StudentDevice>> {
        return studentDeviceDao.getDevicesByGroup(groupId)
    }
    
    /**
     * 刷新教学电源数据
     */
    suspend fun refreshTeachingPowerData(deviceId: String): Result<TeachingPower> {
        return try {
            val response = teachingPowerApi.getTeachingPowerStatus(deviceId)
            if (response.isSuccessful && response.body() != null) {
                val teachingPower = response.body()!!
                teachingPowerDao.insertTeachingPower(teachingPower)
                Result.success(teachingPower)
            } else {
                Result.failure(Exception("Failed to fetch teaching power data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing teaching power data", e)
            Result.failure(e)
        }
    }
    
    /**
     * 刷新学生电源组数据
     */
    suspend fun refreshStudentGroups(): Result<List<StudentPowerGroup>> {
        return try {
            val response = teachingPowerApi.getStudentGroups()
            if (response.isSuccessful && response.body() != null) {
                val groups = response.body()!!
                studentPowerGroupDao.insertGroups(groups)
                Result.success(groups)
            } else {
                Result.failure(Exception("Failed to fetch student groups: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing student groups", e)
            Result.failure(e)
        }
    }
    
    /**
     * 刷新学生设备数据
     */
    suspend fun refreshStudentDevices(groupId: String): Result<List<StudentDevice>> {
        return try {
            val response = teachingPowerApi.getStudentDevices(groupId)
            if (response.isSuccessful && response.body() != null) {
                val devices = response.body()!!
                studentDeviceDao.insertDevices(devices)
                Result.success(devices)
            } else {
                Result.failure(Exception("Failed to fetch student devices: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing student devices", e)
            Result.failure(e)
        }
    }
    
    /**
     * 控制教学电源
     */
    suspend fun controlTeachingPower(deviceId: String, command: PowerControlCommand): Result<Unit> {
        return try {
            // 发送API请求
            val response = teachingPowerApi.sendPowerCommand(deviceId, command)
            if (response.isSuccessful) {
                // 同时通过WebSocket发送实时命令
                webSocketService.sendControlCommand(
                    deviceId = deviceId,
                    command = command.command.name,
                    parameters = mapOf("value" to (command.value ?: ""))
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to control teaching power: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling teaching power", e)
            Result.failure(e)
        }
    }
    
    /**
     * 控制所有设备电源（批量操作）
     */
    suspend fun controlAllDevicesPower(request: PowerControlRequest): Result<Unit> {
        return try {
            // 这里应该调用批量控制的API端点
            // 暂时循环调用单个设备控制
            for (deviceId in request.deviceIds) {
                val command = PowerControlCommand(
                    deviceId = deviceId,
                    command = if (request.action == "on") PowerCommand.ENABLE_LOW_VOLTAGE else PowerCommand.DISABLE_LOW_VOLTAGE,
                    value = request.action
                )
                controlTeachingPower(deviceId, command)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling all devices power", e)
            Result.failure(e)
        }
    }
    
    /**
     * 控制学生组电源
     */
    suspend fun controlStudentGroupPower(groupId: String, command: PowerControlCommand): Result<Unit> {
        return try {
            val response = teachingPowerApi.controlStudentGroupPower(groupId, command)
            if (response.isSuccessful) {
                // 更新本地数据库
                val enabled = when (command.command) {
                    PowerCommand.ENABLE_LOW_VOLTAGE -> true
                    PowerCommand.DISABLE_LOW_VOLTAGE -> false
                    else -> return Result.success(Unit)
                }
                studentDeviceDao.updateGroupPowerStatus(groupId, enabled, System.currentTimeMillis())
                
                // 发送WebSocket命令
                webSocketService.sendControlCommand(
                    deviceId = "group_$groupId",
                    command = command.command.name,
                    parameters = mapOf("groupId" to groupId, "value" to (command.value ?: ""))
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to control student group power: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling student group power", e)
            Result.failure(e)
        }
    }
    
    /**
     * 控制单个学生设备
     */
    suspend fun controlStudentDevice(deviceId: String, command: PowerControlCommand): Result<Unit> {
        return try {
            val response = teachingPowerApi.controlStudentDevice(deviceId, command)
            if (response.isSuccessful) {
                // 发送WebSocket命令
                webSocketService.sendControlCommand(
                    deviceId = deviceId,
                    command = command.command.name,
                    parameters = mapOf("value" to (command.value ?: ""))
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to control student device: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error controlling student device", e)
            Result.failure(e)
        }
    }
    
    /**
     * 初始化模拟数据（开发阶段使用）
     */
    suspend fun initializeSimulatedData() {
        try {
            // 插入模拟教学电源数据
            val teachingPower = TeachingPower(
                deviceId = "teaching-power-01",
                lowVoltageEnabled = false,
                highVoltageEnabled = false,
                acVoltage = 220f,
                dcVoltage = 12f,
                current = 0.5f,
                temperature = 25.5f
            )
            teachingPowerDao.insertTeachingPower(teachingPower)
            
            // 插入模拟学生电源组
            val groups = listOf(
                StudentPowerGroup(
                    groupId = "group-1",
                    groupName = "第1组",
                    deviceCount = 10,
                    enabledCount = 5,
                    totalPower = 120f,
                    avgVoltage = 12f,
                    avgCurrent = 1.2f
                ),
                StudentPowerGroup(
                    groupId = "group-2",
                    groupName = "第2组",
                    deviceCount = 10,
                    enabledCount = 8,
                    totalPower = 180f,
                    avgVoltage = 12f,
                    avgCurrent = 1.5f
                ),
                StudentPowerGroup(
                    groupId = "group-3",
                    groupName = "第3组",
                    deviceCount = 10,
                    enabledCount = 3,
                    totalPower = 60f,
                    avgVoltage = 12f,
                    avgCurrent = 0.8f
                )
            )
            studentPowerGroupDao.insertGroups(groups)
            
            // 插入模拟学生设备
            val devices = mutableListOf<StudentDevice>()
            for (groupIndex in 1..3) {
                for (deviceIndex in 1..10) {
                    devices.add(
                        StudentDevice(
                            deviceId = "student-device-$groupIndex-$deviceIndex",
                            groupId = "group-$groupIndex",
                            studentName = "学生${groupIndex}${deviceIndex}",
                            seatNumber = "${groupIndex}-${deviceIndex}",
                            enabled = deviceIndex <= (groupIndex * 2 + 1),
                            voltage = 12f,
                            current = if (deviceIndex <= (groupIndex * 2 + 1)) 1.2f else 0f,
                            power = if (deviceIndex <= (groupIndex * 2 + 1)) 14.4f else 0f,
                            status = StudentDeviceStatus.NORMAL
                        )
                    )
                }
            }
            studentDeviceDao.insertDevices(devices)
            
            Log.d(TAG, "Initialized simulated teaching power data")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing simulated data", e)
        }
    }
}
