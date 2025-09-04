package com.sslab.hmi.data.api

import com.sslab.hmi.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 设备发现和管理API
 */
interface DeviceApi {
    
    /**
     * 获取所有设备列表
     */
    @GET("api/devices")
    suspend fun getDevices(): Response<List<Device>>
    
    /**
     * 获取特定设备信息
     */
    @GET("api/devices/{deviceId}")
    suspend fun getDevice(@Path("deviceId") deviceId: String): Response<Device>
    
    /**
     * 添加新设备
     */
    @POST("api/devices")
    suspend fun addDevice(@Body request: AddDeviceRequest): Response<ApiResponse<Device>>
    
    /**
     * 更新设备信息
     */
    @PUT("api/devices/{deviceId}")
    suspend fun updateDevice(
        @Path("deviceId") deviceId: String,
        @Body device: Device
    ): Response<Device>
    
    /**
     * 删除设备
     */
    @DELETE("api/devices/{deviceId}")
    suspend fun deleteDevice(@Path("deviceId") deviceId: String): Response<Unit>
    
    /**
     * 发送设备控制命令
     */
    @POST("api/devices/{deviceId}/command")
    suspend fun sendDeviceCommand(
        @Path("deviceId") deviceId: String,
        @Body command: DeviceCommand
    ): Response<Map<String, Any>>
    
    /**
     * 扫描设备
     */
    @POST("api/devices/scan")
    suspend fun scanDevices(): Response<List<Device>>
    
    /**
     * 获取环境数据
     */
    @GET("api/devices/{deviceId}/environment")
    suspend fun getEnvironmentData(@Path("deviceId") deviceId: String): Response<EnvironmentData>
    
    /**
     * 获取电源数据
     */
    @GET("api/devices/{deviceId}/power")
    suspend fun getPowerData(@Path("deviceId") deviceId: String): Response<PowerControlData>
    
    /**
     * 设备心跳检测
     */
    @POST("api/devices/{deviceId}/heartbeat")
    suspend fun deviceHeartbeat(@Path("deviceId") deviceId: String): Response<Unit>
}

/**
 * 教学电源API
 */
interface TeachingPowerApi {
    
    /**
     * 获取教学电源状态
     */
    @GET("api/teaching-power/{deviceId}")
    suspend fun getTeachingPowerStatus(@Path("deviceId") deviceId: String): Response<TeachingPower>
    
    /**
     * 发送电源控制命令
     */
    @POST("api/teaching-power/{deviceId}/control")
    suspend fun sendPowerCommand(
        @Path("deviceId") deviceId: String,
        @Body command: PowerControlCommand
    ): Response<Unit>
    
    /**
     * 获取学生电源组列表
     */
    @GET("api/student-groups")
    suspend fun getStudentGroups(): Response<List<StudentPowerGroup>>
    
    /**
     * 获取学生设备列表
     */
    @GET("api/student-groups/{groupId}/devices")
    suspend fun getStudentDevices(@Path("groupId") groupId: String): Response<List<StudentDevice>>
    
    /**
     * 控制学生组电源
     */
    @POST("api/student-groups/{groupId}/control")
    suspend fun controlStudentGroupPower(
        @Path("groupId") groupId: String,
        @Body command: PowerControlCommand
    ): Response<Unit>
    
    /**
     * 控制单个学生设备
     */
    @POST("api/student-devices/{deviceId}/control")
    suspend fun controlStudentDevice(
        @Path("deviceId") deviceId: String,
        @Body command: PowerControlCommand
    ): Response<Unit>
}

/**
 * 环境监测API
 */
interface EnvironmentApi {
    
    /**
     * 获取环境数据
     */
    @GET("api/environment/{deviceId}")
    suspend fun getEnvironmentData(@Path("deviceId") deviceId: String): Response<EnvironmentData>
    
    /**
     * 获取环境历史数据
     */
    @GET("api/environment/{deviceId}/history")
    suspend fun getEnvironmentHistory(
        @Path("deviceId") deviceId: String,
        @Query("startTime") startTime: Long,
        @Query("endTime") endTime: Long
    ): Response<List<EnvironmentData>>
}

/**
 * 升降台控制API
 */
interface LiftControlApi {
    
    /**
     * 获取升降台状态
     */
    @GET("api/lift/{deviceId}")
    suspend fun getLiftStatus(@Path("deviceId") deviceId: String): Response<LiftControl>
    
    /**
     * 控制升降台
     */
    @POST("api/lift/{deviceId}/control")
    suspend fun controlLift(
        @Path("deviceId") deviceId: String,
        @Body command: ControlCommand
    ): Response<Unit>
}

/**
 * 设备控制API
 */
interface DeviceControlApi {
    
    /**
     * 获取可控制设备列表
     */
    @GET("api/control/devices")
    suspend fun getControllableDevices(): Response<List<DeviceControl>>
    
    /**
     * 获取设备控制状态
     */
    @GET("api/control/{deviceId}")
    suspend fun getDeviceControlStatus(@Path("deviceId") deviceId: String): Response<DeviceControl>
    
    /**
     * 发送设备控制命令
     */
    @POST("api/control/{deviceId}")
    suspend fun sendControlCommand(
        @Path("deviceId") deviceId: String,
        @Body command: ControlCommand
    ): Response<Unit>
}
