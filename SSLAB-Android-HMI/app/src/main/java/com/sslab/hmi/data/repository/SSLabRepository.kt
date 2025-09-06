package com.sslab.hmi.data.repository

import com.sslab.hmi.data.model.*
import com.sslab.hmi.data.network.SSLabApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SSLAB设备管理数据仓库
 * 封装API调用逻辑，提供统一的数据访问接口
 */
@Singleton
class SSLabRepository @Inject constructor(
    private val apiService: SSLabApiService
) {
    
    /**
     * 提取API响应中的错误信息
     */
    private fun extractErrorMessage(response: Response<*>, defaultMessage: String): String {
        return try {
            (response.body() as? ApiResponse<*>)?.error ?: defaultMessage
        } catch (e: Exception) {
            defaultMessage
        }
    }
    
    // ========== 设备管理相关 ==========
    
    /**
     * 获取所有设备列表
     */
    suspend fun getDevices(): Result<List<Device>> {
        return try {
            val response = apiService.getDevices()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(extractErrorMessage(response, "获取设备列表失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID获取设备信息
     */
    suspend fun getDevice(deviceId: String): Result<Device> {
        return try {
            val response = apiService.getDevice(deviceId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { device ->
                    Result.success(device)
                } ?: Result.failure(Exception("设备数据为空"))
            } else {
                Result.failure(Exception(extractErrorMessage(response, "获取设备信息失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 添加新设备
     */
    suspend fun addDevice(request: AddDeviceRequest): Result<Device> {
        return try {
            val response = apiService.addDevice(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { device ->
                    Result.success(device)
                } ?: Result.failure(Exception("添加设备返回数据为空"))
            } else {
                Result.failure(Exception(extractErrorMessage(response, "添加设备失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新设备信息
     */
    suspend fun updateDevice(deviceId: String, request: UpdateDeviceRequest): Result<Device> {
        return try {
            val response = apiService.updateDevice(deviceId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { device ->
                    Result.success(device)
                } ?: Result.failure(Exception("更新设备返回数据为空"))
            } else {
                Result.failure(Exception(extractErrorMessage(response, "更新设备失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除设备
     */
    suspend fun deleteDevice(deviceId: String): Result<Unit> {
        return try {
            val response = apiService.deleteDevice(deviceId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(extractErrorMessage(response, "删除设备失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 设备控制
     */
    suspend fun controlDevice(deviceId: String, command: DeviceCommand): Result<Map<String, Any>> {
        return try {
            val response = apiService.sendDeviceCommand(deviceId, command)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyMap())
            } else {
                Result.failure(Exception(extractErrorMessage(response, "设备控制失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 数据监控相关 ==========
    
    /**
     * 获取环境数据
     */
    suspend fun getEnvironmentData(deviceId: String): Result<EnvironmentData> {
        return try {
            val response = apiService.getEnvironmentData(deviceId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { data ->
                    Result.success(data)
                } ?: Result.failure(Exception("环境数据为空"))
            } else {
                Result.failure(Exception(extractErrorMessage(response, "获取环境数据失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取电源数据
     */
    suspend fun getPowerData(deviceId: String): Result<PowerControlData> {
        return try {
            val response = apiService.getPowerData(deviceId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { data ->
                    Result.success(data)
                } ?: Result.failure(Exception("电源数据为空"))
            } else {
                Result.failure(Exception(extractErrorMessage(response, "获取电源数据失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 系统管理相关 ==========
    
    /**
     * 获取系统统计信息
     */
    suspend fun getStats(): Result<SystemStats> {
        return try {
            val response = apiService.getStats()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { stats ->
                    Result.success(stats)
                } ?: Result.failure(Exception("系统统计数据为空"))
            } else {
                Result.failure(Exception(extractErrorMessage(response, "获取系统统计失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取设备统计信息
     */
    suspend fun getDeviceStats(): Result<DeviceStats> {
        return try {
            val response = apiService.getDeviceStats()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { stats ->
                    Result.success(stats)
                } ?: Result.failure(Exception("设备统计数据为空"))
            } else {
                Result.failure(Exception(extractErrorMessage(response, "获取设备统计失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建测试设备
     */
    suspend fun createTestDevices(request: CreateTestDevicesRequest): Result<String> {
        return try {
            val response = apiService.createTestDevices(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: "创建成功")
            } else {
                Result.failure(Exception(extractErrorMessage(response, "创建测试设备失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 清空所有设备
     */
    suspend fun clearDevices(): Result<String> {
        return try {
            val response = apiService.clearDevices()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: "清空成功")
            } else {
                Result.failure(Exception(extractErrorMessage(response, "清空设备失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取系统健康状态
     */
    suspend fun getSystemHealth(): Result<SystemHealth> {
        return try {
            val response = apiService.getSystemHealth()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { health ->
                    Result.success(health)
                } ?: Result.failure(Exception("系统健康状态数据为空"))
            } else {
                Result.failure(Exception(extractErrorMessage(response, "获取系统健康状态失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 扫描设备
     */
    suspend fun scanDevices(): Result<List<Device>> {
        return try {
            val response = apiService.scanDevices()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(extractErrorMessage(response, "设备扫描失败")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
