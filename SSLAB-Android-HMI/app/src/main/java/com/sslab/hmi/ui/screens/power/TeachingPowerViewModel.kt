package com.sslab.hmi.ui.screens.power

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sslab.hmi.data.model.*
import com.sslab.hmi.data.repository.TeachingPowerRepository
import com.sslab.hmi.data.websocket.WebSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeachingPowerViewModel @Inject constructor(
    private val teachingPowerRepository: TeachingPowerRepository,
    private val webSocketService: WebSocketService
) : ViewModel() {
    
    companion object {
        private const val TEACHING_POWER_DEVICE_ID = "teaching-power-01"
    }
    
    // UI状态
    private val _uiState = MutableStateFlow(TeachingPowerUiState())
    val uiState: StateFlow<TeachingPowerUiState> = _uiState.asStateFlow()
    
    // 教学电源状态
    val teachingPowerStatus = teachingPowerRepository.getTeachingPowerStatus(TEACHING_POWER_DEVICE_ID)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    // 学生电源组
    val studentGroups = teachingPowerRepository.getStudentGroups()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 学生设备（当前选中组）
    private val _selectedGroupId = MutableStateFlow("group-1")
    val selectedGroupId: StateFlow<String> = _selectedGroupId.asStateFlow()
    
    val studentDevices = selectedGroupId.flatMapLatest { groupId ->
        teachingPowerRepository.getStudentDevices(groupId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        initializeData()
        observeWebSocketUpdates()
    }
    
    /**
     * 初始化数据
     */
    private fun initializeData() {
        viewModelScope.launch {
            // 初始化模拟数据
            teachingPowerRepository.initializeSimulatedData()
            
            // 刷新数据
            refreshAllData()
            
            // 连接WebSocket
            webSocketService.connect()
        }
    }
    
    /**
     * 监听WebSocket更新
     */
    private fun observeWebSocketUpdates() {
        viewModelScope.launch {
            webSocketService.teachingPowerUpdates.collect { powerData ->
                // WebSocket数据会自动更新到数据库，这里可以处理UI特定逻辑
            }
        }
    }
    
    /**
     * 刷新所有数据
     */
    fun refreshAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 刷新教学电源数据
                teachingPowerRepository.refreshTeachingPowerData(TEACHING_POWER_DEVICE_ID)
                
                // 刷新学生组数据
                teachingPowerRepository.refreshStudentGroups()
                
                // 刷新当前选中组的学生设备
                teachingPowerRepository.refreshStudentDevices(_selectedGroupId.value)
                
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "刷新数据失败"
                )
            }
        }
    }
    
    /**
     * 切换低压电源
     */
    fun toggleLowVoltage() {
        viewModelScope.launch {
            val command = if (teachingPowerStatus.value?.lowVoltageEnabled == true) {
                PowerControlCommand(TEACHING_POWER_DEVICE_ID, PowerCommand.DISABLE_LOW_VOLTAGE)
            } else {
                PowerControlCommand(TEACHING_POWER_DEVICE_ID, PowerCommand.ENABLE_LOW_VOLTAGE)
            }
            
            val result = teachingPowerRepository.controlTeachingPower(TEACHING_POWER_DEVICE_ID, command)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "控制失败"
                )
            }
        }
    }
    
    /**
     * 设置低压电压值
     */
    fun setLowVoltageValue(value: Float) {
        viewModelScope.launch {
            val command = PowerControlCommand(TEACHING_POWER_DEVICE_ID, PowerCommand.SET_VOLTAGE, value)
            val result = teachingPowerRepository.controlTeachingPower(TEACHING_POWER_DEVICE_ID, command)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "设置电压失败"
                )
            }
        }
    }
    
    /**
     * 切换高压电源
     */
    fun toggleHighVoltage() {
        viewModelScope.launch {
            val command = if (teachingPowerStatus.value?.highVoltageEnabled == true) {
                PowerControlCommand(TEACHING_POWER_DEVICE_ID, PowerCommand.DISABLE_HIGH_VOLTAGE)
            } else {
                PowerControlCommand(TEACHING_POWER_DEVICE_ID, PowerCommand.ENABLE_HIGH_VOLTAGE)
            }
            
            val result = teachingPowerRepository.controlTeachingPower(TEACHING_POWER_DEVICE_ID, command)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "控制失败"
                )
            }
        }
    }
    
    /**
     * 设置高压电压值
     */
    fun setHighVoltageValue(value: Float) {
        viewModelScope.launch {
            val command = PowerControlCommand(TEACHING_POWER_DEVICE_ID, PowerCommand.SET_VOLTAGE, value)
            val result = teachingPowerRepository.controlTeachingPower(TEACHING_POWER_DEVICE_ID, command)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "设置电压失败"
                )
            }
        }
    }
    
    /**
     * 切换学生组电源
     */
    fun toggleGroupPower(groupId: String) {
        viewModelScope.launch {
            val group = studentGroups.value.find { it.groupId == groupId }
            val command = if (group?.enabledCount ?: 0 > 0) {
                PowerControlCommand(groupId, PowerCommand.DISABLE_LOW_VOLTAGE)
            } else {
                PowerControlCommand(groupId, PowerCommand.ENABLE_LOW_VOLTAGE)
            }
            
            val result = teachingPowerRepository.controlStudentGroupPower(groupId, command)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "控制学生组失败"
                )
            }
        }
    }
    
    /**
     * 控制单个学生设备
     */
    fun toggleStudentDevice(deviceId: String) {
        viewModelScope.launch {
            val device = studentDevices.value.find { it.deviceId == deviceId }
            val command = if (device?.enabled == true) {
                PowerControlCommand(deviceId, PowerCommand.DISABLE_LOW_VOLTAGE)
            } else {
                PowerControlCommand(deviceId, PowerCommand.ENABLE_LOW_VOLTAGE)
            }
            
            val result = teachingPowerRepository.controlStudentDevice(deviceId, command)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "控制学生设备失败"
                )
            }
        }
    }
    
    /**
     * 选择学生组
     */
    fun selectGroup(groupId: String) {
        _selectedGroupId.value = groupId
        viewModelScope.launch {
            teachingPowerRepository.refreshStudentDevices(groupId)
        }
    }
    
    /**
     * 紧急停止所有电源
     */
    fun emergencyStop() {
        viewModelScope.launch {
            val command = PowerControlCommand(TEACHING_POWER_DEVICE_ID, PowerCommand.EMERGENCY_STOP)
            teachingPowerRepository.controlTeachingPower(TEACHING_POWER_DEVICE_ID, command)
            
            // 停止所有学生组电源
            studentGroups.value.forEach { group ->
                val groupCommand = PowerControlCommand(group.groupId, PowerCommand.EMERGENCY_STOP)
                teachingPowerRepository.controlStudentGroupPower(group.groupId, groupCommand)
            }
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
}
    
    fun toggleMode() {
        _uiState.value = _uiState.value.copy(isDCMode = !_uiState.value.isDCMode)
    }
    
    fun toggleHighVoltage() {
        _uiState.value = _uiState.value.copy(isHighVoltageEnabled = !_uiState.value.isHighVoltageEnabled)
    }
    
    fun toggleHighCurrent() {
        _uiState.value = _uiState.value.copy(isHighCurrentEnabled = !_uiState.value.isHighCurrentEnabled)
    }
    
    fun toggleSocket() {
        _uiState.value = _uiState.value.copy(isSocketEnabled = !_uiState.value.isSocketEnabled)
    }
    
    fun updateHighVoltageDuration(duration: String) {
        _uiState.value = _uiState.value.copy(highVoltageDuration = duration)
    }
    
    fun updateHighCurrentDuration(duration: String) {
        _uiState.value = _uiState.value.copy(highCurrentDuration = duration)
    }
    
    fun toggleSync() {
        _uiState.value = _uiState.value.copy(isSyncEnabled = !_uiState.value.isSyncEnabled)
    }
    
    fun toggleStudentGroup(group: String) {
        val currentGroups = _uiState.value.studentGroups.toMutableMap()
        currentGroups[group] = !(currentGroups[group] ?: false)
        _uiState.value = _uiState.value.copy(studentGroups = currentGroups)
    }
    
    fun syncParametersToStudents() {
        viewModelScope.launch {
            // TODO: 实现参数同步逻辑
            // 1. 收集当前电源参数
            // 2. 发送到所有启用的学生组设备
            // 3. 更新最后同步时间
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _uiState.value = _uiState.value.copy(lastSyncTime = currentTime)
        }
    }
}

data class TeachingPowerUiState(
    // 低压电源状态
    val realTimeVoltage: Float = 12.5f,
    val outputVoltage: String = "15",
    val realTimeCurrent: Float = 2.0f,
    val currentLimit: String = "2.5",
    val isOutputEnabled: Boolean = true,
    val isDCMode: Boolean = true,
    
    // 高压电源状态
    val highVoltageRange: String = "240V-300V",
    val isHighVoltageEnabled: Boolean = false,
    val highVoltageDuration: String = "10",
    val isHighCurrentEnabled: Boolean = false,
    val highCurrentDuration: String = "10",
    val isSocketEnabled: Boolean = false,
    
    // 学生电源控制
    val isSyncEnabled: Boolean = true,
/**
 * 教学电源UI状态
 */
data class TeachingPowerUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
