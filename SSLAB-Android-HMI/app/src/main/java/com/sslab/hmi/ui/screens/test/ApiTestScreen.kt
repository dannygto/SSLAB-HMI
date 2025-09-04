package com.sslab.hmi.ui.screens.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sslab.hmi.data.model.*
import com.sslab.hmi.data.repository.SSLabRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * API测试界面
 * 用于验证与Web平台的API连接
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTestScreen(
    viewModel: ApiTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.testGetDevices() },
                modifier = Modifier.weight(1f)
            ) {
                Text("获取设备")
            }
            
            Button(
                onClick = { viewModel.testGetStats() },
                modifier = Modifier.weight(1f)
            ) {
                Text("获取统计")
            }
            
            Button(
                onClick = { viewModel.testSystemHealth() },
                modifier = Modifier.weight(1f)
            ) {
                Text("系统状态")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 状态显示
        when (uiState) {
            is ApiTestUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is ApiTestUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "API调用失败",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            is ApiTestUiState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "API调用成功",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.data,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            is ApiTestUiState.DeviceList -> {
                Text(
                    text = "设备列表 (${uiState.devices.size} 个设备)",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.devices) { device ->
                        DeviceItem(device = device)
                    }
                }
            }
            
            is ApiTestUiState.Idle -> {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "API连接测试",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击上方按钮测试与SSLAB设备模拟器的API连接",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "服务器地址: http://localhost:8080",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 快速操作按钮
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.testCreateTestDevices() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("创建测试设备 (100个)")
            }
            
            OutlinedButton(
                onClick = { viewModel.testClearDevices() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("清空所有设备")
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: Device
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Badge {
                    Text(text = device.status.name)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "类型: ${device.type.displayName}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "分组: ${device.groupId.ifEmpty { "未分组" }}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * API测试ViewModel
 */
@HiltViewModel
class ApiTestViewModel @Inject constructor(
    private val repository: SSLabRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ApiTestUiState>(ApiTestUiState.Idle)
    val uiState: StateFlow<ApiTestUiState> = _uiState.asStateFlow()
    
    /**
     * 测试获取设备列表
     */
    fun testGetDevices() {
        viewModelScope.launch {
            _uiState.value = ApiTestUiState.Loading
            repository.getDevices()
                .onSuccess { devices ->
                    _uiState.value = ApiTestUiState.DeviceList(devices)
                }
                .onFailure { throwable ->
                    _uiState.value = ApiTestUiState.Error(
                        throwable.message ?: "获取设备列表失败"
                    )
                }
        }
    }
    
    /**
     * 测试获取统计信息
     */
    fun testGetStats() {
        viewModelScope.launch {
            _uiState.value = ApiTestUiState.Loading
            repository.getStats()
                .onSuccess { stats ->
                    val message = """
                        设备总数: ${stats.deviceCount}
                        在线设备: ${stats.onlineDevices}
                        离线设备: ${stats.offlineDevices}
                        分组数量: ${stats.groupCount}
                        连接客户端: ${stats.connectedClients}
                        系统运行时间: ${stats.systemUptime}
                        内存使用: ${stats.memoryUsage.usagePercent}%
                    """.trimIndent()
                    _uiState.value = ApiTestUiState.Success(message)
                }
                .onFailure { throwable ->
                    _uiState.value = ApiTestUiState.Error(
                        throwable.message ?: "获取统计信息失败"
                    )
                }
        }
    }
    
    /**
     * 测试系统健康状态
     */
    fun testSystemHealth() {
        viewModelScope.launch {
            _uiState.value = ApiTestUiState.Loading
            repository.getSystemHealth()
                .onSuccess { health ->
                    val message = """
                        系统状态: ${health.status}
                        服务器状态: ${health.server.status}
                        运行时间: ${health.server.uptimeFormatted}
                        平台: ${health.system.platform}
                        Node版本: ${health.system.nodeVersion}
                        CPU使用率: ${health.system.cpuUsage}%
                        内存使用: ${health.system.memory.usagePercent}%
                        设备数量: ${health.application.deviceCount}
                        分组数量: ${health.application.groupCount}
                        连接客户端: ${health.application.connectedClients}
                    """.trimIndent()
                    _uiState.value = ApiTestUiState.Success(message)
                }
                .onFailure { throwable ->
                    _uiState.value = ApiTestUiState.Error(
                        throwable.message ?: "获取系统健康状态失败"
                    )
                }
        }
    }
    
    /**
     * 测试创建测试设备
     */
    fun testCreateTestDevices() {
        viewModelScope.launch {
            _uiState.value = ApiTestUiState.Loading
            val request = CreateTestDevicesRequest(
                count = 100,
                createGroups = true,
                groupCount = 4
            )
            repository.createTestDevices(request)
                .onSuccess { message ->
                    _uiState.value = ApiTestUiState.Success(message)
                }
                .onFailure { throwable ->
                    _uiState.value = ApiTestUiState.Error(
                        throwable.message ?: "创建测试设备失败"
                    )
                }
        }
    }
    
    /**
     * 测试清空设备
     */
    fun testClearDevices() {
        viewModelScope.launch {
            _uiState.value = ApiTestUiState.Loading
            repository.clearDevices()
                .onSuccess { message ->
                    _uiState.value = ApiTestUiState.Success(message)
                }
                .onFailure { throwable ->
                    _uiState.value = ApiTestUiState.Error(
                        throwable.message ?: "清空设备失败"
                    )
                }
        }
    }
}

/**
 * API测试UI状态
 */
sealed class ApiTestUiState {
    object Idle : ApiTestUiState()
    object Loading : ApiTestUiState()
    data class Success(val data: String) : ApiTestUiState()
    data class Error(val message: String) : ApiTestUiState()
    data class DeviceList(val devices: List<Device>) : ApiTestUiState()
}
