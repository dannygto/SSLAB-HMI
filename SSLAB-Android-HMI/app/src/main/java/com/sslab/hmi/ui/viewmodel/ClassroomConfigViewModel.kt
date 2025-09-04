package com.sslab.hmi.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sslab.hmi.data.model.ClassroomConfig
import com.sslab.hmi.data.model.Device
import com.sslab.hmi.data.model.DeviceDiscoveryConfig
import com.sslab.hmi.data.model.GroupDeviceStats
import com.sslab.hmi.data.repository.ClassroomConfigRepository
import com.sslab.hmi.data.discovery.DeviceDiscoveryService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 教室配置界面ViewModel
 */
@HiltViewModel
class ClassroomConfigViewModel @Inject constructor(
    private val classroomConfigRepository: ClassroomConfigRepository,
    private val deviceDiscoveryService: DeviceDiscoveryService
) : ViewModel() {
    
    companion object {
        private const val TAG = "ClassroomConfigViewModel"
    }
    
    // UI状态
    data class UiState(
        val currentConfig: ClassroomConfig = ClassroomConfig(),
        val isConfigured: Boolean = false,
        val discoveryConfig: DeviceDiscoveryConfig = DeviceDiscoveryConfig(""),
        val groupStats: GroupDeviceStats = GroupDeviceStats(),
        val groupDevices: List<Device> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        observeClassroomConfig()
        observeDeviceDiscovery()
    }
    
    /**
     * 观察教室配置变化
     */
    private fun observeClassroomConfig() {
        viewModelScope.launch {
            combine(
                classroomConfigRepository.currentConfig,
                classroomConfigRepository.isConfigured,
                classroomConfigRepository.discoveryConfig
            ) { config, isConfigured, discoveryConfig ->
                Triple(config, isConfigured, discoveryConfig)
            }.collect { (config, isConfigured, discoveryConfig) ->
                _uiState.value = _uiState.value.copy(
                    currentConfig = config,
                    isConfigured = isConfigured,
                    discoveryConfig = discoveryConfig
                )
                
                Log.d(TAG, "Config updated: $config, configured: $isConfigured")
            }
        }
    }
    
    /**
     * 观察设备发现状态
     */
    private fun observeDeviceDiscovery() {
        viewModelScope.launch {
            combine(
                deviceDiscoveryService.groupStats,
                deviceDiscoveryService.discoveredDevices
            ) { stats, devices ->
                Pair(stats, devices)
            }.collect { (stats, devices) ->
                // 过滤当前分组的设备
                val currentGroupId = _uiState.value.currentConfig.groupId
                val groupDevices = if (currentGroupId.isNotEmpty()) {
                    devices.filter { it.groupId == currentGroupId }
                } else {
                    emptyList()
                }
                
                _uiState.value = _uiState.value.copy(
                    groupStats = stats,
                    groupDevices = groupDevices
                )
                
                Log.d(TAG, "Discovery updated - Stats: $stats, Group devices: ${groupDevices.size}")
            }
        }
    }
    
    /**
     * 保存配置
     */
    fun saveConfig(config: ClassroomConfig) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                // 验证配置
                val errors = classroomConfigRepository.validateConfig(config)
                if (errors.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "配置错误：${errors.joinToString(", ")}"
                    )
                    return@launch
                }
                
                // 保存配置
                classroomConfigRepository.saveClassroomConfig(config)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
                
                Log.d(TAG, "Config saved successfully: $config")
                
                // 重新开始设备发现
                startDeviceDiscovery()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save config", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "保存配置失败：${e.message}"
                )
            }
        }
    }
    
    /**
     * 开始设备发现
     */
    fun startDeviceDiscovery() {
        viewModelScope.launch {
            try {
                val groupId = _uiState.value.currentConfig.groupId
                if (groupId.isNotEmpty()) {
                    Log.d(TAG, "Starting device discovery for group: $groupId")
                    deviceDiscoveryService.startDiscovery()
                } else {
                    Log.w(TAG, "Cannot start discovery - no group ID configured")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "请先配置分组标识"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start discovery", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "启动设备发现失败：${e.message}"
                )
            }
        }
    }
    
    /**
     * 停止设备发现
     */
    fun stopDeviceDiscovery() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Stopping device discovery")
                deviceDiscoveryService.stopDiscovery()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop discovery", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "停止设备发现失败：${e.message}"
                )
            }
        }
    }
    
    /**
     * 刷新分组设备
     */
    fun refreshGroupDevices() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing group devices")
                deviceDiscoveryService.refreshDevices()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh devices", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "刷新设备失败：${e.message}"
                )
            }
        }
    }
    
    /**
     * 重置配置
     */
    fun resetConfig() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Resetting classroom config")
                classroomConfigRepository.resetConfig()
                deviceDiscoveryService.stopDiscovery()
                
                _uiState.value = _uiState.value.copy(
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reset config", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "重置配置失败：${e.message}"
                )
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * 生成建议的分组ID
     */
    fun generateSuggestedGroupId(buildingCode: String, floorNumber: Int, roomNumber: String): String {
        return classroomConfigRepository.generateSuggestedGroupId(buildingCode, floorNumber, roomNumber)
    }
}
