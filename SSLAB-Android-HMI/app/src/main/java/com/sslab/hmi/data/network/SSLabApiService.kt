package com.sslab.hmi.data.network

import com.sslab.hmi.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * SSLAB设备管理API接口
 */
interface SSLabApiService {
    
    /**
     * 获取所有设备列表
     */
    @GET("api/devices")
    suspend fun getDevices(): Response<ApiResponse<List<Device>>>
    
    /**
     * 通过ID获取特定设备
     */
    @GET("api/devices/{id}")
    suspend fun getDevice(@Path("id") deviceId: String): Response<ApiResponse<Device>>
    
    /**
     * 添加新设备
     */
    @POST("api/devices")
    suspend fun addDevice(@Body device: Device): Response<ApiResponse<Device>>
    
    /**
     * 更新设备信息
     */
    @PUT("api/devices/{id}")
    suspend fun updateDevice(
        @Path("id") deviceId: String,
        @Body device: Device
    ): Response<ApiResponse<Device>>
    
    /**
     * 删除设备
     */
    @DELETE("api/devices/{id}")
    suspend fun deleteDevice(@Path("id") deviceId: String): Response<ApiResponse<Unit>>
    
    /**
     * 发现网络中的设备
     */
    @POST("api/discovery/scan")
    suspend fun scanDevices(): Response<ApiResponse<DeviceDiscoveryResult>>
    
    /**
     * 获取设备状态
     */
    @GET("api/devices/{id}/status")
    suspend fun getDeviceStatus(@Path("id") deviceId: String): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * 发送设备控制命令
     */
    @POST("api/devices/{id}/control")
    suspend fun sendDeviceCommand(
        @Path("id") deviceId: String,
        @Body command: DeviceCommand
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * 获取所有设备类型
     */
    @GET("api/info/device-types")
    suspend fun getDeviceTypes(): Response<ApiResponse<List<String>>>
    
    /**
     * 获取设备类型能力
     */
    @GET("api/info/device-capabilities/{type}")
    suspend fun getDeviceCapabilities(@Path("type") type: String): Response<ApiResponse<DeviceCapabilities>>
    
    /**
     * 扫描发现设备
     */
    @POST("api/discovery/scan")
    suspend fun scanForDevices(): Response<ApiResponse<DeviceDiscoveryResult>>
    
    /**
     * 获取系统统计信息
     */
    @GET("api/stats")
    suspend fun getSystemStats(): Response<ApiResponse<SystemStats>>
    
    /**
     * 获取所有分组
     */
    @GET("api/groups")
    suspend fun getGroups(): Response<ApiResponse<List<DeviceGroup>>>
    
    /**
     * 创建新分组
     */
    @POST("api/groups")
    suspend fun createGroup(@Body group: CreateGroupRequest): Response<ApiResponse<DeviceGroup>>
    
    /**
     * 批量控制分组设备
     */
    @POST("api/groups/{groupId}/control")
    suspend fun controlGroupDevices(
        @Path("groupId") groupId: String,
        @Body command: DeviceCommand
    ): Response<ApiResponse<GroupControlResult>>
}

/**
 * 批量命令请求
 */
data class BulkCommandRequest(
    val deviceIds: List<String>,
    val command: DeviceCommand
)
