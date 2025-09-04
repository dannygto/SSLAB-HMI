package com.sslab.hmi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sslab.hmi.data.model.Device
import com.sslab.hmi.data.model.DeviceType
import com.sslab.hmi.ui.components.DeviceCard
import com.sslab.hmi.ui.viewmodel.DeviceViewModel
import kotlinx.coroutines.launch

/**
 * 设备管理主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    onNavigateToDeviceDetail: (String) -> Unit,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 收集状态
    val devices by viewModel.filteredDevices.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDeviceType by viewModel.selectedDeviceType.collectAsState()
    val selectedDevices by viewModel.selectedDevices.collectAsState()
    
    // Toast消息
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            // 这里可以显示Toast或Snackbar
            // 为了简化，暂时省略Toast实现
        }
    }
    
    var showAddDeviceDialog by remember { mutableStateOf(false) }
    var showScanningDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部状态栏
        TopStatusBar(
            isConnected = isConnected,
            onlineDevices = devices.count { it.status == "ONLINE" },
            totalDevices = devices.size,
            onRefresh = { viewModel.refreshDevices() },
            onScan = { 
                showScanningDialog = true
                viewModel.scanForDevices()
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 搜索和过滤栏
        SearchAndFilterBar(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::setSearchQuery,
            selectedDeviceType = selectedDeviceType,
            onDeviceTypeChange = viewModel::setDeviceTypeFilter
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 批量操作栏
        if (selectedDevices.isNotEmpty()) {
            BulkOperationBar(
                selectedCount = selectedDevices.size,
                onSelectAll = { viewModel.selectAllDevices(true) },
                onDeselectAll = { viewModel.selectAllDevices(false) },
                onBulkPowerOn = { 
                    viewModel.sendBulkCommand(selectedDevices.toList(), "power_on")
                },
                onBulkPowerOff = { 
                    viewModel.sendBulkCommand(selectedDevices.toList(), "power_off")
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 设备列表
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            errorMessage != null -> {
                ErrorDisplay(
                    message = errorMessage ?: "",
                    onRetry = { viewModel.refreshDevices() },
                    onDismiss = { viewModel.clearErrorMessage() }
                )
            }
            
            devices.isEmpty() -> {
                EmptyDeviceList(
                    onAddDevice = { showAddDeviceDialog = true },
                    onScanDevices = { 
                        showScanningDialog = true
                        viewModel.scanForDevices()
                    }
                )
            }
            
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = devices,
                        key = { it.id }
                    ) { device ->
                        DeviceCard(
                            device = device,
                            isSelected = selectedDevices.contains(device.id),
                            onSelectionChange = { isSelected ->
                                viewModel.selectDevice(device.id, isSelected)
                            },
                            onClick = { onNavigateToDeviceDetail(device.id) },
                            onPowerToggle = { 
                                val command = if (device.isOnline) "power_off" else "power_on"
                                viewModel.sendDeviceCommand(device.id, command)
                            },
                            onDelete = { viewModel.deleteDevice(device.id) }
                        )
                    }
                }
            }
        }
    }
    
    // 添加设备浮动按钮
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showAddDeviceDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加设备")
        }
    }
    
    // 对话框
    if (showAddDeviceDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDeviceDialog = false },
            onAddDevice = { device ->
                viewModel.addDevice(device)
                showAddDeviceDialog = false
            }
        )
    }
    
    if (showScanningDialog) {
        ScanningDialog(
            onDismiss = { showScanningDialog = false }
        )
    }
}

/**
 * 顶部状态栏
 */
@Composable
private fun TopStatusBar(
    isConnected: Boolean,
    onlineDevices: Int,
    totalDevices: Int,
    onRefresh: () -> Unit,
    onScan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isConnected) "已连接" else "未连接",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "在线设备: $onlineDevices / $totalDevices",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row {
                IconButton(onClick = onScan) {
                    Icon(Icons.Default.Search, contentDescription = "扫描设备")
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        }
    }
}

/**
 * 搜索和过滤栏
 */
@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedDeviceType: String?,
    onDeviceTypeChange: (String?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 搜索框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("搜索设备...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        
        // 设备类型过滤下拉框
        var expanded by remember { mutableStateOf(false) }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedDeviceType?.let { type ->
                    DeviceType.values().find { it.apiValue == type }?.displayName ?: type
                } ?: "所有类型",
                onValueChange = { },
                readOnly = true,
                trailingIcon = { 
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
                },
                modifier = Modifier.menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("所有类型") },
                    onClick = {
                        onDeviceTypeChange(null)
                        expanded = false
                    }
                )
                DeviceType.values().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            onDeviceTypeChange(type.apiValue)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 批量操作栏
 */
@Composable
private fun BulkOperationBar(
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onBulkPowerOn: () -> Unit,
    onBulkPowerOff: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "已选择 $selectedCount 个设备",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row {
                TextButton(onClick = onSelectAll) {
                    Text("全选")
                }
                TextButton(onClick = onDeselectAll) {
                    Text("取消")
                }
                TextButton(onClick = onBulkPowerOn) {
                    Text("开启")
                }
                TextButton(onClick = onBulkPowerOff) {
                    Text("关闭")
                }
            }
        }
    }
}

/**
 * 错误显示
 */
@Composable
private fun ErrorDisplay(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
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
                text = "连接错误",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                TextButton(onClick = onRetry) {
                    Text("重试")
                }
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
        }
    }
}

/**
 * 空设备列表
 */
@Composable
private fun EmptyDeviceList(
    onAddDevice: () -> Unit,
    onScanDevices: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.DevicesOther,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "没有发现设备",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "添加设备或扫描网络中的设备",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row {
                OutlinedButton(onClick = onAddDevice) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加设备")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(onClick = onScanDevices) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("扫描设备")
                }
            }
        }
    }
}

/**
 * 添加设备对话框
 */
@Composable
private fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onAddDevice: (Device) -> Unit
) {
    // 这里实现添加设备的对话框
    // 为了简化，先显示一个简单的对话框
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加设备") },
        text = { Text("添加设备功能正在开发中...") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

/**
 * 扫描设备对话框
 */
@Composable
private fun ScanningDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("扫描设备") },
        text = { 
            Column {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("正在扫描网络中的设备...")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
