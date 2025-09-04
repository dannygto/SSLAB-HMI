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
                Result.failure(Exception(response.body()?.error ?: "获取设备列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取设备列表作为Flow
     */
    fun getDevicesFlow(): Flow<Result<List<Device>>> = flow {
        emit(getDevices())
    }
    
    /**
     * 根据ID获取设备
     */
    suspend fun getDevice(deviceId: String): Result<Device> {
        return try {
            val response = apiService.getDevice(deviceId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { device ->
                    Result.success(device)
                } ?: Result.failure(Exception("设备数据为空"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取设备信息失败"))
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
                Result.failure(Exception(response.body()?.error ?: "添加设备失败"))
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
                Result.failure(Exception(response.body()?.error ?: "更新设备失败"))
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
                Result.failure(Exception(response.body()?.error ?: "删除设备失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 控制单个设备
     */
    suspend fun controlDevice(deviceId: String, command: DeviceControlCommand): Result<Map<String, Any>> {
        return try {
            val response = apiService.controlDevice(deviceId, command)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyMap())
            } else {
                Result.failure(Exception(response.body()?.error ?: "设备控制失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量控制设备
     */
    suspend fun controlDevicesBatch(request: BatchControlRequest): Result<Map<String, Any>> {
        return try {
            val response = apiService.controlDevicesBatch(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyMap())
            } else {
                Result.failure(Exception(response.body()?.error ?: "批量控制设备失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 搜索设备
     */
    suspend fun searchDevices(request: DeviceSearchRequest): Result<List<Device>> {
        return try {
            val response = apiService.searchDevices(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "搜索设备失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取分组设备
     */
    suspend fun getDevicesByGroup(groupId: String): Result<List<Device>> {
        return try {
            val response = apiService.getDevicesByGroup(groupId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取分组设备失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 分组管理相关 ==========
    
    /**
     * 获取所有分组
     */
    suspend fun getGroups(): Result<List<DeviceGroup>> {
        return try {
            val response = apiService.getGroups()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取分组列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建分组
     */
    suspend fun createGroup(request: CreateGroupRequest): Result<DeviceGroup> {
        return try {
            val response = apiService.createGroup(request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { group ->
                    Result.success(group)
                } ?: Result.failure(Exception("创建分组返回数据为空"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "创建分组失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新分组
     */
    suspend fun updateGroup(groupId: String, request: UpdateGroupRequest): Result<DeviceGroup> {
        return try {
            val response = apiService.updateGroup(groupId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { group ->
                    Result.success(group)
                } ?: Result.failure(Exception("更新分组返回数据为空"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "更新分组失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除分组
     */
    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            val response = apiService.deleteGroup(groupId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error ?: "删除分组失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 统计信息相关 ==========
    
    /**
     * 获取系统统计信息
     */
    suspend fun getStats(): Result<SystemStats> {
        return try {
            val response = apiService.getStats()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { stats ->
                    Result.success(stats)
                } ?: Result.failure(Exception("统计信息数据为空"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取统计信息失败"))
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
                } ?: Result.failure(Exception("设备统计信息数据为空"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取设备统计信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取分组统计信息
     */
    suspend fun getGroupStats(): Result<GroupStats> {
        return try {
            val response = apiService.getGroupStats()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { stats ->
                    Result.success(stats)
                } ?: Result.failure(Exception("分组统计信息数据为空"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取分组统计信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 系统管理相关 ==========
    
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
                Result.failure(Exception(response.body()?.error ?: "获取系统健康状态失败"))
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
                Result.success(response.body()?.data ?: "测试设备创建成功")
            } else {
                Result.failure(Exception(response.body()?.error ?: "创建测试设备失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 重置系统
     */
    suspend fun resetSystem(): Result<String> {
        return try {
            val response = apiService.resetSystem()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: "系统重置成功")
            } else {
                Result.failure(Exception(response.body()?.error ?: "系统重置失败"))
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
                Result.success(response.body()?.data ?: "设备清空成功")
            } else {
                Result.failure(Exception(response.body()?.error ?: "清空设备失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== 设备信息相关 ==========
    
    /**
     * 获取设备类型列表
     */
    suspend fun getDeviceTypes(): Result<List<DeviceTypeInfo>> {
        return try {
            val response = apiService.getDeviceTypes()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取设备类型失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取设备能力信息
     */
    suspend fun getDeviceCapabilities(type: String): Result<DeviceCapabilities> {
        return try {
            val response = apiService.getDeviceCapabilities(type)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { capabilities ->
                    Result.success(capabilities)
                } ?: Result.failure(Exception("设备能力信息数据为空"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取设备能力信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取API文档
     */
    suspend fun getApiDocs(): Result<ApiDocumentation> {
        return try {
            val response = apiService.getApiDocs()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.let { docs ->
                    Result.success(docs)
                } ?: Result.failure(Exception("API文档数据为空"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "获取API文档失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 结果包装类
 */
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String, val throwable: Throwable? = null) : ApiResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : ApiResult<T>()
}
