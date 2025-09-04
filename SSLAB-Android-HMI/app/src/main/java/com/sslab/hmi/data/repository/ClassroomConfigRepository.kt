package com.sslab.hmi.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.sslab.hmi.data.model.ClassroomConfig
import com.sslab.hmi.data.model.DeviceDiscoveryConfig
import com.sslab.hmi.data.discovery.DeviceDiscoveryService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 教室配置管理服务
 */
@Singleton
class ClassroomConfigRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceDiscoveryService: DeviceDiscoveryService
) {
    
    companion object {
        private const val TAG = "ClassroomConfigRepository"
        private const val PREFS_NAME = "classroom_config"
        private const val KEY_GROUP_ID = "group_id"
        private const val KEY_CLASSROOM_NAME = "classroom_name"
        private const val KEY_BUILDING_NAME = "building_name"
        private const val KEY_FLOOR_NUMBER = "floor_number"
        private const val KEY_ROOM_NUMBER = "room_number"
        private const val KEY_TEACHER_NAME = "teacher_name"
        private const val KEY_IS_CONFIGURED = "is_configured"
    }
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // 当前教室配置
    private val _currentConfig = MutableStateFlow(loadConfigFromPrefs())
    val currentConfig: StateFlow<ClassroomConfig> = _currentConfig.asStateFlow()
    
    // 设备发现配置
    private val _discoveryConfig = MutableStateFlow(DeviceDiscoveryConfig(getCurrentGroupId()))
    val discoveryConfig: StateFlow<DeviceDiscoveryConfig> = _discoveryConfig.asStateFlow()
    
    // 配置状态
    private val _isConfigured = MutableStateFlow(sharedPrefs.getBoolean(KEY_IS_CONFIGURED, false))
    val isConfigured: StateFlow<Boolean> = _isConfigured.asStateFlow()
    
    init {
        // 初始化设备发现服务的分组ID
        val groupId = getCurrentGroupId()
        if (groupId.isNotEmpty()) {
            deviceDiscoveryService.setGroupId(groupId)
        }
    }
    
    /**
     * 获取当前分组ID
     */
    fun getCurrentGroupId(): String {
        return sharedPrefs.getString(KEY_GROUP_ID, "") ?: ""
    }
    
    /**
     * 保存教室配置
     */
    fun saveClassroomConfig(config: ClassroomConfig) {
        Log.d(TAG, "Saving classroom config: $config")
        
        sharedPrefs.edit().apply {
            putString(KEY_GROUP_ID, config.groupId)
            putString(KEY_CLASSROOM_NAME, config.classroomName)
            putString(KEY_BUILDING_NAME, config.buildingName)
            putInt(KEY_FLOOR_NUMBER, config.floorNumber)
            putString(KEY_ROOM_NUMBER, config.roomNumber)
            putString(KEY_TEACHER_NAME, config.teacherName)
            putBoolean(KEY_IS_CONFIGURED, true)
            apply()
        }
        
        _currentConfig.value = config
        _isConfigured.value = true
        
        // 更新设备发现服务的分组ID
        deviceDiscoveryService.setGroupId(config.groupId)
        
        // 更新发现配置
        _discoveryConfig.value = _discoveryConfig.value.copy(groupId = config.groupId)
    }
    
    /**
     * 更新分组ID
     */
    fun updateGroupId(groupId: String) {
        Log.d(TAG, "Updating group ID to: $groupId")
        
        val currentConfig = _currentConfig.value
        val updatedConfig = currentConfig.copy(
            groupId = groupId,
            lastUpdateTime = System.currentTimeMillis()
        )
        
        saveClassroomConfig(updatedConfig)
    }
    
    /**
     * 重置配置
     */
    fun resetConfig() {
        Log.d(TAG, "Resetting classroom config")
        
        sharedPrefs.edit().clear().apply()
        _currentConfig.value = ClassroomConfig()
        _isConfigured.value = false
        _discoveryConfig.value = DeviceDiscoveryConfig("")
    }
    
    /**
     * 从SharedPreferences加载配置
     */
    private fun loadConfigFromPrefs(): ClassroomConfig {
        return ClassroomConfig(
            groupId = sharedPrefs.getString(KEY_GROUP_ID, "") ?: "",
            classroomName = sharedPrefs.getString(KEY_CLASSROOM_NAME, "") ?: "",
            buildingName = sharedPrefs.getString(KEY_BUILDING_NAME, "") ?: "",
            floorNumber = sharedPrefs.getInt(KEY_FLOOR_NUMBER, 1),
            roomNumber = sharedPrefs.getString(KEY_ROOM_NUMBER, "") ?: "",
            teacherName = sharedPrefs.getString(KEY_TEACHER_NAME, "") ?: "",
            isActive = sharedPrefs.getBoolean(KEY_IS_CONFIGURED, false)
        )
    }
    
    /**
     * 验证配置是否完整
     */
    fun validateConfig(config: ClassroomConfig): List<String> {
        val errors = mutableListOf<String>()
        
        if (config.groupId.isEmpty()) {
            errors.add("分组标识不能为空")
        } else if (!config.groupId.matches(Regex("^[A-Z]{2}\\d{3}$"))) {
            errors.add("分组标识格式应为：两个大写字母+三位数字，如HX001")
        }
        
        if (config.classroomName.isEmpty()) {
            errors.add("教室名称不能为空")
        }
        
        if (config.buildingName.isEmpty()) {
            errors.add("楼栋名称不能为空")
        }
        
        if (config.roomNumber.isEmpty()) {
            errors.add("房间号不能为空")
        }
        
        return errors
    }
    
    /**
     * 生成建议的分组ID
     */
    fun generateSuggestedGroupId(buildingCode: String, floorNumber: Int, roomNumber: String): String {
        val building = buildingCode.take(2).uppercase()
        val floor = floorNumber.toString().padStart(1, '0')
        val room = roomNumber.filter { it.isDigit() }.take(2).padStart(2, '0')
        return "$building$floor$room"
    }
}
