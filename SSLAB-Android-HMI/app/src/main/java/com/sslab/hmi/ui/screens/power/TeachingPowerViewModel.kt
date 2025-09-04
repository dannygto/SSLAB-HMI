package com.sslab.hmi.ui.screens.power

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sslab.hmi.data.model.Device
import com.sslab.hmi.data.model.PowerControlCommand
import com.sslab.hmi.data.model.PowerControlRequest
import com.sslab.hmi.data.model.PowerCommand
import com.sslab.hmi.data.model.StudentGroup
import com.sslab.hmi.data.model.StudentPowerGroup
import com.sslab.hmi.data.model.StudentDevice
import com.sslab.hmi.data.repository.TeachingPowerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeachingPowerViewModel @Inject constructor(
    private val repository: TeachingPowerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeachingPowerUiState())
    val uiState: StateFlow<TeachingPowerUiState> = _uiState.asStateFlow()

    // 额外的StateFlow属性用于UI绑定
    private val _teachingPowerStatus = MutableStateFlow(TeachingPowerStatus())
    val teachingPowerStatus: StateFlow<TeachingPowerStatus> = _teachingPowerStatus.asStateFlow()

    private val _studentGroups = MutableStateFlow<List<StudentPowerGroup>>(emptyList())
    val studentGroups: StateFlow<List<StudentPowerGroup>> = _studentGroups.asStateFlow()

    private val _studentDevices = MutableStateFlow<List<StudentDevice>>(emptyList())
    val studentDevices: StateFlow<List<StudentDevice>> = _studentDevices.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String>("")
    val selectedGroupId: StateFlow<String> = _selectedGroupId.asStateFlow()

    // 同步状态管理
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        loadDevices()
        loadStudentGroups()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val devices = repository.getDevices()
                _uiState.value = _uiState.value.copy(
                    devices = devices,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun loadStudentGroups() {
        viewModelScope.launch {
            try {
                val groups = repository.getStudentGroups()
                _uiState.value = _uiState.value.copy(studentGroups = groups)
                // 转换为StudentPowerGroup类型
                val powerGroups = groups.map { group ->
                    convertToStudentPowerGroup(group)
                }
                _studentGroups.value = powerGroups
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun convertToStudentPowerGroup(group: StudentGroup): StudentPowerGroup {
        return StudentPowerGroup(
            groupId = group.id,
            groupName = group.name,
            deviceCount = group.deviceIds.size,
            enabledCount = if (group.isPowerOn) group.deviceIds.size else 0,
            totalPower = 0f, // 实际应用中需要计算
            avgVoltage = 0f, // 实际应用中需要计算
            avgCurrent = 0f  // 实际应用中需要计算
        )
    }

    private fun convertToStudentDevice(device: Device): StudentDevice {
        return StudentDevice(
            deviceId = device.id,
            groupId = device.groupId,
            studentName = "", // 实际应用中需要从其他源获取
            seatNumber = "", // 实际应用中需要从其他源获取
            enabled = device.isOnline,
            voltage = 0f, // 实际应用中需要从设备状态获取
            current = 0f, // 实际应用中需要从设备状态获取
            power = 0f   // 实际应用中需要计算
        )
    }

    fun toggleAllDevicesPower() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val newState = !_uiState.value.allDevicesOn
                val request = PowerControlRequest(
                    deviceIds = _uiState.value.devices.map { it.id },
                    action = if (newState) "on" else "off"
                )
                repository.controlAllDevicesPower(request)
                
                _uiState.value = _uiState.value.copy(
                    allDevicesOn = newState,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun toggleStudentGroupPower(groupId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val currentGroups = _uiState.value.studentGroups.toMutableList()
                val groupIndex = currentGroups.indexOfFirst { it.id == groupId }
                
                if (groupIndex != -1) {
                    val group = currentGroups[groupIndex]
                    val newPowerState = !group.isPowerOn
                    
                    val command = PowerControlCommand(
                        deviceId = groupId,
                        command = if (newPowerState) PowerCommand.ENABLE_LOW_VOLTAGE else PowerCommand.DISABLE_LOW_VOLTAGE,
                        value = if (newPowerState) "on" else "off"
                    )
                    repository.controlStudentGroupPower(groupId, command)
                    
                    currentGroups[groupIndex] = group.copy(isPowerOn = newPowerState)
                    _uiState.value = _uiState.value.copy(
                        studentGroups = currentGroups,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        loadDevices()
        loadStudentGroups()
    }

    // 新增的方法用于UI绑定
    fun setLowVoltageValue(value: Float) {
        _teachingPowerStatus.value = _teachingPowerStatus.value.copy(lowVoltageValue = value)
    }

    fun toggleLowVoltage() {
        val current = _teachingPowerStatus.value
        _teachingPowerStatus.value = current.copy(lowVoltageEnabled = !current.lowVoltageEnabled)
    }

    fun setHighVoltageValue(value: Float) {
        _teachingPowerStatus.value = _teachingPowerStatus.value.copy(highVoltageValue = value)
    }

    fun toggleHighVoltage() {
        val current = _teachingPowerStatus.value
        _teachingPowerStatus.value = current.copy(highVoltageEnabled = !current.highVoltageEnabled)
    }

    fun selectGroup(groupId: String) {
        _selectedGroupId.value = groupId
        // 根据选择的分组更新学生设备列表
        val groupDevices = _uiState.value.devices.filter { device ->
            device.groupId == groupId
        }
        // 转换为StudentDevice类型
        val studentDevices = groupDevices.map { device ->
            convertToStudentDevice(device)
        }
        _studentDevices.value = studentDevices
    }

    fun toggleGroupPower(groupId: String) {
        toggleStudentGroupPower(groupId)
    }

    fun toggleStudentDevice(deviceId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val device = _uiState.value.devices.find { it.id == deviceId }
                if (device != null) {
                    val newState = !device.isOnline
                    val request = PowerControlRequest(
                        deviceIds = listOf(deviceId),
                        action = if (newState) "on" else "off"
                    )
                    repository.controlAllDevicesPower(request)
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun emergencyStop() {
        viewModelScope.launch {
            try {
                _teachingPowerStatus.value = _teachingPowerStatus.value.copy(emergencyStopActive = true)
                _uiState.value = _uiState.value.copy(isLoading = true)
                val request = PowerControlRequest(
                    deviceIds = _uiState.value.devices.map { it.id },
                    action = "emergency_stop"
                )
                repository.controlAllDevicesPower(request)
                _uiState.value = _uiState.value.copy(
                    allDevicesOn = false,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refreshAllData() {
        refresh()
    }

    // 同步所有学生设备
    fun syncAllStudentDevices() {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // 模拟同步过程
                kotlinx.coroutines.delay(2000) // 2秒同步时间
                
                // 同步逻辑
                loadStudentGroups()
                loadStudentDevices()
                
                _uiState.value = _uiState.value.copy(isLoading = false)
                _isSyncing.value = false
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
                _isSyncing.value = false
            }
        }
    }

    // 加载学生设备
    private fun loadStudentDevices() {
        viewModelScope.launch {
            try {
                // 模拟加载学生设备数据
                val devices = listOf(
                    com.sslab.hmi.data.model.StudentDevice(
                        deviceId = "device1",
                        groupId = "group1", 
                        studentName = "学生1",
                        seatNumber = "座位1",
                        enabled = true
                    ),
                    com.sslab.hmi.data.model.StudentDevice(
                        deviceId = "device2",
                        groupId = "group1",
                        studentName = "学生2", 
                        seatNumber = "座位2",
                        enabled = false
                    ),
                    com.sslab.hmi.data.model.StudentDevice(
                        deviceId = "device3",
                        groupId = "group1",
                        studentName = "学生3",
                        seatNumber = "座位3", 
                        enabled = true
                    )
                )
                _studentDevices.value = devices
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // 低压设置相关函数
    private val _lowVoltageSettings = MutableStateFlow(
        LowVoltageSettings(targetVoltage = "5.0", targetCurrent = "1.0", enabled = false)
    )
    val lowVoltageSettings: StateFlow<LowVoltageSettings?> = _lowVoltageSettings.asStateFlow()

    fun updateLowVoltage(voltage: String) {
        val current = _lowVoltageSettings.value
        _lowVoltageSettings.value = current.copy(targetVoltage = voltage)
    }

    fun updateLowCurrent(current: String) {
        val currentValue = _lowVoltageSettings.value
        _lowVoltageSettings.value = currentValue.copy(targetCurrent = current)
    }

    fun enableLowVoltage(enabled: Boolean) {
        val current = _lowVoltageSettings.value
        _lowVoltageSettings.value = current.copy(enabled = enabled)
    }

    // 设置低压电源交流/直流模式
    fun setLowVoltageAcMode(isAcMode: Boolean) {
        val current = _lowVoltageSettings.value
        _lowVoltageSettings.value = current.copy(isAcMode = isAcMode)
    }

    // 切换低压电源交流/直流模式
    fun toggleLowVoltageMode(isAcMode: Boolean) {
        val current = _lowVoltageSettings.value
        _lowVoltageSettings.value = current.copy(isAcMode = isAcMode)
    }

    // 高压设置相关函数
    private val _highVoltageSettings = MutableStateFlow(
        HighVoltageSettings(targetVoltage = "240.0", targetCurrent = "10.0", enabled = false, is300VMode = false)
    )
    val highVoltageSettings: StateFlow<HighVoltageSettings?> = _highVoltageSettings.asStateFlow()

    fun updateHighVoltage(voltage: String) {
        val current = _highVoltageSettings.value
        _highVoltageSettings.value = current.copy(targetVoltage = voltage)
    }

    fun updateHighCurrent(current: String) {
        val currentValue = _highVoltageSettings.value
        _highVoltageSettings.value = currentValue.copy(targetCurrent = current)
    }

    fun enableHighVoltage(enabled: Boolean) {
        val current = _highVoltageSettings.value
        _highVoltageSettings.value = current.copy(enabled = enabled, autoShutdownSeconds = 10)
        
        // 如果启用高压，启动10秒倒计时
        if (enabled) {
            viewModelScope.launch {
                for (countdown in 10 downTo 1) {
                    val currentState = _highVoltageSettings.value
                    if (!currentState.enabled) break // 如果手动关闭，停止倒计时
                    
                    _highVoltageSettings.value = currentState.copy(autoShutdownSeconds = countdown)
                    kotlinx.coroutines.delay(1000) // 1秒延迟
                }
                
                // 倒计时结束，自动关闭
                val finalState = _highVoltageSettings.value
                if (finalState.enabled) {
                    _highVoltageSettings.value = finalState.copy(enabled = false, autoShutdownSeconds = 10)
                }
            }
        } else {
            // 手动关闭时重置倒计时
            _highVoltageSettings.value = current.copy(autoShutdownSeconds = 10)
        }
    }

    // 切换高压电源240V/300V模式
    fun toggleHighVoltageMode(is300V: Boolean) {
        val current = _highVoltageSettings.value
        val newVoltage = if (is300V) "300.0" else "240.0"
        _highVoltageSettings.value = current.copy(
            is300VMode = is300V,
            targetVoltage = newVoltage
        )
    }

    // 大电流电源9V40A控制
    private val _highCurrentSettings = MutableStateFlow(
        HighCurrentSettings(voltage = "9.0", current = "40.0", enabled = false)
    )
    val highCurrentSettings: StateFlow<HighCurrentSettings> = _highCurrentSettings.asStateFlow()

    fun enableHighCurrent(enabled: Boolean) {
        val current = _highCurrentSettings.value
        _highCurrentSettings.value = current.copy(enabled = enabled, autoShutdownSeconds = 10)
        
        // 如果启用大电流，启动10秒倒计时
        if (enabled) {
            viewModelScope.launch {
                for (countdown in 10 downTo 1) {
                    val currentState = _highCurrentSettings.value
                    if (!currentState.enabled) break // 如果手动关闭，停止倒计时
                    
                    _highCurrentSettings.value = currentState.copy(autoShutdownSeconds = countdown)
                    kotlinx.coroutines.delay(1000) // 1秒延迟
                }
                
                // 倒计时结束，自动关闭
                val finalState = _highCurrentSettings.value
                if (finalState.enabled) {
                    _highCurrentSettings.value = finalState.copy(enabled = false, autoShutdownSeconds = 10)
                }
            }
        } else {
            // 手动关闭时重置倒计时
            _highCurrentSettings.value = current.copy(autoShutdownSeconds = 10)
        }
    }
}

data class TeachingPowerUiState(
    val devices: List<Device> = emptyList(),
    val studentGroups: List<StudentGroup> = emptyList(),
    val allDevicesOn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class TeachingPowerStatus(
    val lowVoltageEnabled: Boolean = false,
    val lowVoltageValue: Float = 0f,
    val highVoltageEnabled: Boolean = false,
    val highVoltageValue: Float = 0f,
    val emergencyStopActive: Boolean = false,
    val dcVoltage: Float = 0f,
    val current: Float = 0f,
    val acVoltage: Float = 220f
)

// 低压设置数据类
data class LowVoltageSettings(
    val targetVoltage: String,
    val targetCurrent: String,
    val currentVoltage: Float = 0.0f,
    val currentCurrent: Float = 0.0f,
    val enabled: Boolean,
    val isAcMode: Boolean = false,  // 交流模式(true)或直流模式(false)
    val isEnabled: Boolean = enabled  // 为了兼容两种命名方式
)

// 高压设置数据类
data class HighVoltageSettings(
    val targetVoltage: String,
    val targetCurrent: String,
    val currentVoltage: Float = 0.0f,
    val currentCurrent: Float = 0.0f,
    val enabled: Boolean,
    val is300VMode: Boolean = false,  // 300V模式(true)或240V模式(false)
    val autoShutdownSeconds: Int = 10,  // 10秒自动关断
    val isEnabled: Boolean = enabled  // 为了兼容两种命名方式
)

// 大电流电源设置数据类 (9V40A)
data class HighCurrentSettings(
    val voltage: String = "9.0",  // 固定9V
    val current: String = "40.0", // 固定40A
    val enabled: Boolean = false,
    val autoShutdownSeconds: Int = 10  // 10秒自动关断
)
