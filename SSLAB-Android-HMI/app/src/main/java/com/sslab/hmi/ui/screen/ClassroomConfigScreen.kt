package com.sslab.hmi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sslab.hmi.data.model.ClassroomConfig
import com.sslab.hmi.data.model.DeviceType
import com.sslab.hmi.ui.viewmodel.ClassroomConfigViewModel

/**
 * 教室配置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: ClassroomConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfigDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("教室配置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showConfigDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "配置")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 当前配置卡片
            CurrentConfigCard(
                config = uiState.currentConfig,
                isConfigured = uiState.isConfigured,
                onEditClick = { showConfigDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 设备发现状态
            DeviceDiscoveryCard(
                discoveryConfig = uiState.discoveryConfig,
                groupStats = uiState.groupStats,
                onStartDiscovery = { viewModel.startDeviceDiscovery() },
                onStopDiscovery = { viewModel.stopDeviceDiscovery() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 分组设备列表
            GroupDevicesCard(
                devices = uiState.groupDevices,
                onRefresh = { viewModel.refreshGroupDevices() }
            )
        }
    }
    
    // 配置对话框
    if (showConfigDialog) {
        ClassroomConfigDialog(
            config = uiState.currentConfig,
            onDismiss = { showConfigDialog = false },
            onSave = { config ->
                viewModel.saveConfig(config)
                showConfigDialog = false
            }
        )
    }
}

@Composable
private fun CurrentConfigCard(
    config: ClassroomConfig,
    isConfigured: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当前配置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isConfigured && config.groupId.isNotEmpty()) {
                ConfigInfoItem("分组标识", config.groupId)
                ConfigInfoItem("教室名称", config.classroomName)
                ConfigInfoItem("楼栋", config.buildingName)
                ConfigInfoItem("楼层", "${config.floorNumber}层")
                ConfigInfoItem("房间号", config.roomNumber)
                if (config.teacherName.isNotEmpty()) {
                    ConfigInfoItem("授课教师", config.teacherName)
                }
            } else {
                Text(
                    text = "尚未配置教室信息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConfigInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DeviceDiscoveryCard(
    discoveryConfig: com.sslab.hmi.data.model.DeviceDiscoveryConfig,
    groupStats: com.sslab.hmi.data.model.GroupDeviceStats,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "设备发现",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onStartDiscovery) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "开始发现")
                    }
                    IconButton(onClick = onStopDiscovery) {
                        Icon(Icons.Default.Stop, contentDescription = "停止发现")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ConfigInfoItem("目标分组", discoveryConfig.groupId.ifEmpty { "未设置" })
            ConfigInfoItem("发现设备", "${groupStats.totalDevices}台")
            ConfigInfoItem("在线设备", "${groupStats.onlineDevices}台")
            ConfigInfoItem("离线设备", "${groupStats.offlineDevices}台")
        }
    }
}

@Composable
private fun GroupDevicesCard(
    devices: List<com.sslab.hmi.data.model.Device>,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分组设备",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (devices.isEmpty()) {
                Text(
                    text = "暂无设备",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(devices) { device ->
                        DeviceItem(device = device)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(device: com.sslab.hmi.data.model.Device) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${DeviceType.values().find { it.apiValue == device.type }?.displayName ?: "未知设备"} | ${device.ipAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        val statusColor = when (device.status) {
            "ONLINE" -> MaterialTheme.colorScheme.primary
            "OFFLINE" -> MaterialTheme.colorScheme.error
            "CONNECTING" -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.outline
        }
        
        Text(
            text = when (device.status) {
                "ONLINE" -> "在线"
                "OFFLINE" -> "离线"  
                "CONNECTING" -> "连接中"
                else -> "未知"
            },
            style = MaterialTheme.typography.bodySmall,
            color = statusColor
        )
    }
}

@Composable
private fun ClassroomConfigDialog(
    config: ClassroomConfig,
    onDismiss: () -> Unit,
    onSave: (ClassroomConfig) -> Unit
) {
    var groupId by remember { mutableStateOf(config.groupId) }
    var classroomName by remember { mutableStateOf(config.classroomName) }
    var buildingName by remember { mutableStateOf(config.buildingName) }
    var floorNumber by remember { mutableStateOf(config.floorNumber.toString()) }
    var roomNumber by remember { mutableStateOf(config.roomNumber) }
    var teacherName by remember { mutableStateOf(config.teacherName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("配置教室信息") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = groupId,
                        onValueChange = { groupId = it.uppercase() },
                        label = { Text("分组标识") },
                        placeholder = { Text("例如：HX001") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = classroomName,
                        onValueChange = { classroomName = it },
                        label = { Text("教室名称") },
                        placeholder = { Text("例如：智能硬件实验室") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = buildingName,
                        onValueChange = { buildingName = it },
                        label = { Text("楼栋名称") },
                        placeholder = { Text("例如：海心楼") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = floorNumber,
                        onValueChange = { floorNumber = it },
                        label = { Text("楼层") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = roomNumber,
                        onValueChange = { roomNumber = it },
                        label = { Text("房间号") },
                        placeholder = { Text("例如：101") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("授课教师（可选）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newConfig = config.copy(
                        groupId = groupId,
                        classroomName = classroomName,
                        buildingName = buildingName,
                        floorNumber = floorNumber.toIntOrNull() ?: 1,
                        roomNumber = roomNumber,
                        teacherName = teacherName,
                        lastUpdateTime = System.currentTimeMillis()
                    )
                    onSave(newConfig)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
