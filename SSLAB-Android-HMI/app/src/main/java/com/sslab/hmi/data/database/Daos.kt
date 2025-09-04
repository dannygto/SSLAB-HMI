package com.sslab.hmi.data.database

import androidx.room.*
import com.sslab.hmi.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 设备DAO
 */
@Dao
interface DeviceDao {
    
    @Query("SELECT * FROM devices ORDER BY lastUpdateTime DESC")
    fun getAllDevices(): Flow<List<Device>>
    
    @Query("SELECT * FROM devices WHERE type = :type")
    fun getDevicesByType(type: DeviceType): Flow<List<Device>>
    
    @Query("SELECT * FROM devices WHERE id = :deviceId")
    suspend fun getDevice(deviceId: String): Device?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<Device>)
    
    @Update
    suspend fun updateDevice(device: Device)
    
    @Delete
    suspend fun deleteDevice(device: Device)
    
    @Query("DELETE FROM devices WHERE id = :deviceId")
    suspend fun deleteDeviceById(deviceId: String)
    
    @Query("UPDATE devices SET status = :status, lastUpdateTime = :timestamp WHERE id = :deviceId")
    suspend fun updateDeviceStatus(deviceId: String, status: DeviceStatus, timestamp: Long)
}

/**
 * 教学电源DAO
 */
@Dao
interface TeachingPowerDao {
    
    @Query("SELECT * FROM teaching_power ORDER BY lastUpdateTime DESC")
    fun getAllTeachingPower(): Flow<List<TeachingPower>>
    
    @Query("SELECT * FROM teaching_power WHERE deviceId = :deviceId")
    suspend fun getTeachingPower(deviceId: String): TeachingPower?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachingPower(teachingPower: TeachingPower)
    
    @Update
    suspend fun updateTeachingPower(teachingPower: TeachingPower)
    
    @Delete
    suspend fun deleteTeachingPower(teachingPower: TeachingPower)
}

/**
 * 学生电源组DAO
 */
@Dao
interface StudentPowerGroupDao {
    
    @Query("SELECT * FROM student_power_groups ORDER BY groupName")
    fun getAllGroups(): Flow<List<StudentPowerGroup>>
    
    @Query("SELECT * FROM student_power_groups WHERE groupId = :groupId")
    suspend fun getGroup(groupId: String): StudentPowerGroup?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: StudentPowerGroup)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<StudentPowerGroup>)
    
    @Update
    suspend fun updateGroup(group: StudentPowerGroup)
    
    @Delete
    suspend fun deleteGroup(group: StudentPowerGroup)
}

/**
 * 学生设备DAO
 */
@Dao
interface StudentDeviceDao {
    
    @Query("SELECT * FROM student_devices WHERE groupId = :groupId ORDER BY seatNumber")
    fun getDevicesByGroup(groupId: String): Flow<List<StudentDevice>>
    
    @Query("SELECT * FROM student_devices WHERE deviceId = :deviceId")
    suspend fun getDevice(deviceId: String): StudentDevice?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: StudentDevice)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<StudentDevice>)
    
    @Update
    suspend fun updateDevice(device: StudentDevice)
    
    @Delete
    suspend fun deleteDevice(device: StudentDevice)
    
    @Query("UPDATE student_devices SET enabled = :enabled, lastUpdateTime = :timestamp WHERE groupId = :groupId")
    suspend fun updateGroupPowerStatus(groupId: String, enabled: Boolean, timestamp: Long)
}

/**
 * 环境数据DAO
 */
@Dao
interface EnvironmentDataDao {
    
    @Query("SELECT * FROM environment_data WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentData(deviceId: String, limit: Int = 100): Flow<List<EnvironmentData>>
    
    @Query("SELECT * FROM environment_data WHERE deviceId = :deviceId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getDataInRange(deviceId: String, startTime: Long, endTime: Long): Flow<List<EnvironmentData>>
    
    @Query("SELECT * FROM environment_data WHERE deviceId = :deviceId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestData(deviceId: String): EnvironmentData?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(data: EnvironmentData)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataList(dataList: List<EnvironmentData>)
    
    @Query("DELETE FROM environment_data WHERE deviceId = :deviceId AND timestamp < :beforeTime")
    suspend fun deleteOldData(deviceId: String, beforeTime: Long)
}

/**
 * 升降台控制DAO
 */
@Dao
interface LiftControlDao {
    
    @Query("SELECT * FROM lift_control ORDER BY lastUpdateTime DESC")
    fun getAllLiftControls(): Flow<List<LiftControl>>
    
    @Query("SELECT * FROM lift_control WHERE deviceId = :deviceId")
    suspend fun getLiftControl(deviceId: String): LiftControl?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiftControl(liftControl: LiftControl)
    
    @Update
    suspend fun updateLiftControl(liftControl: LiftControl)
    
    @Delete
    suspend fun deleteLiftControl(liftControl: LiftControl)
}

/**
 * 设备控制DAO
 */
@Dao
interface DeviceControlDao {
    
    @Query("SELECT * FROM device_control ORDER BY deviceName")
    fun getAllDeviceControls(): Flow<List<DeviceControl>>
    
    @Query("SELECT * FROM device_control WHERE deviceId = :deviceId")
    suspend fun getDeviceControl(deviceId: String): DeviceControl?
    
    @Query("SELECT * FROM device_control WHERE controlType = :type")
    fun getDeviceControlsByType(type: ControlType): Flow<List<DeviceControl>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceControl(deviceControl: DeviceControl)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceControls(deviceControls: List<DeviceControl>)
    
    @Update
    suspend fun updateDeviceControl(deviceControl: DeviceControl)
    
    @Delete
    suspend fun deleteDeviceControl(deviceControl: DeviceControl)
}
