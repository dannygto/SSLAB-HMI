package com.sslab.hmi.data.network

import com.sslab.hmi.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * SSLAB设备管理API接口
 * 与Web平台API完全对齐 (http://localhost:8080/api)
 */
interface SSLabApiService {
    
    // ========== 设备管理API ==========
    
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
    suspend fun addDevice(@Body request: AddDeviceRequest): Response<ApiResponse<Device>>
    
    /**
     * 更新设备信息
     */
    @PUT("api/devices/{id}")
    suspend fun updateDevice(
        @Path("id") deviceId: String,
        @Body request: UpdateDeviceRequest
    ): Response<ApiResponse<Device>>
    
    /**
     * 删除设备
     */
    @DELETE("api/devices/{id}")
    suspend fun deleteDevice(@Path("id") deviceId: String): Response<ApiResponse<Unit>>
    
    /**
     * 控制单个设备
     */
    @POST("api/devices/{id}/control")
    suspend fun controlDevice(
        @Path("id") deviceId: String,
        @Body command: DeviceControlCommand
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * 批量控制设备
     */
    @POST("api/devices/control/batch")
    suspend fun controlDevicesBatch(
        @Body request: BatchControlRequest
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * 搜索设备
     */
    @POST("api/devices/search")
    suspend fun searchDevices(
        @Body request: DeviceSearchRequest
    ): Response<ApiResponse<List<Device>>>
    
    /**
     * 获取分组设备
     */
    @GET("api/devices/group/{groupId}")
    suspend fun getDevicesByGroup(@Path("groupId") groupId: String): Response<ApiResponse<List<Device>>>
    
    // ========== 分组管理API ==========
    
    /**
     * 获取所有分组
     */
    @GET("api/groups")
    suspend fun getGroups(): Response<ApiResponse<List<DeviceGroup>>>
    
    /**
     * 创建新分组
     */
    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<ApiResponse<DeviceGroup>>
    
    /**
     * 更新分组
     */
    @PUT("api/groups/{groupId}")
    suspend fun updateGroup(
        @Path("groupId") groupId: String,
        @Body request: UpdateGroupRequest
    ): Response<ApiResponse<DeviceGroup>>
    
    /**
     * 删除分组
     */
    @DELETE("api/groups/{groupId}")
    suspend fun deleteGroup(@Path("groupId") groupId: String): Response<ApiResponse<Unit>>
    
    // ========== 统计信息API ==========
    
    /**
     * 获取系统统计信息
     */
    @GET("api/stats")
    suspend fun getStats(): Response<ApiResponse<SystemStats>>
    
    /**
     * 获取设备统计信息
     */
    @GET("api/stats/devices")
    suspend fun getDeviceStats(): Response<ApiResponse<DeviceStats>>
    
    /**
     * 获取分组统计信息
     */
    @GET("api/stats/groups")
    suspend fun getGroupStats(): Response<ApiResponse<GroupStats>>
    
    // ========== 系统管理API ==========
    
    /**
     * 获取系统健康状态
     */
    @GET("api/system/health")
    suspend fun getSystemHealth(): Response<ApiResponse<SystemHealth>>
    
    /**
     * 重置系统
     */
    @POST("api/system/reset")
    suspend fun resetSystem(): Response<ApiResponse<String>>
    
    /**
     * 清空所有设备
     */
    @POST("api/system/clear")
    suspend fun clearDevices(): Response<ApiResponse<String>>
    
    /**
     * 创建测试设备
     */
    @POST("api/system/create-test-devices")
    suspend fun createTestDevices(@Body request: CreateTestDevicesRequest): Response<ApiResponse<String>>
    
    // ========== 设备信息API ==========
    
    /**
     * 获取所有设备类型
     */
    @GET("api/info/device-types")
    suspend fun getDeviceTypes(): Response<ApiResponse<List<DeviceTypeInfo>>>
    
    /**
     * 获取设备类型能力
     */
    @GET("api/info/device-capabilities/{type}")
    suspend fun getDeviceCapabilities(@Path("type") type: String): Response<ApiResponse<DeviceCapabilities>>
    
    /**
     * 获取API文档
     */
    @GET("api/info/api-docs")
    suspend fun getApiDocs(): Response<ApiResponse<ApiDocumentation>>
    
    /**
     * 批量控制分组设备
     */
    @POST("api/groups/{groupId}/control")
    suspend fun controlGroupDevices(
        @Path("groupId") groupId: String,
        @Body command: DeviceCommand
    ): Response<ApiResponse<GroupControlResult>>
    
    /**
     * 发送设备控制命令
     */
    @POST("api/devices/{deviceId}/command")
    suspend fun sendDeviceCommand(
        @Path("deviceId") deviceId: String,
        @Body command: DeviceCommand
    ): Response<ApiResponse<Map<String, Any>>>
    
    /**
     * 扫描设备
     */
    @POST("api/devices/scan")
    suspend fun scanDevices(): Response<ApiResponse<List<Device>>>
    
    /**
     * 获取环境数据
     */
    @GET("api/devices/{deviceId}/environment")
    suspend fun getEnvironmentData(@Path("deviceId") deviceId: String): Response<ApiResponse<EnvironmentData>>
    
    /**
     * 获取电源数据
     */
    @GET("api/devices/{deviceId}/power")
    suspend fun getPowerData(@Path("deviceId") deviceId: String): Response<ApiResponse<PowerControlData>>
}

/**
 * 批量命令请求
 */
data class BulkCommandRequest(
    val deviceIds: List<String>,
    val command: DeviceCommand
)
