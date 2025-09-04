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
