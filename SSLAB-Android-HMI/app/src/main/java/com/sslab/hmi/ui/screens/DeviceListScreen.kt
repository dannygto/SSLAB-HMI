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
 * 设备管理主界面 - 1280*800横屏优化布局
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDeviceDetail: (String) -> Unit = {},
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "设备管理",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        
        // 1280*800 横屏布局
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 左侧控制面板 (30%)
            Card(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 连接状态
                    ConnectionStatusCard(
                        isConnected = isConnected,
                        onToggleConnection = {
                            // 简化：暂时禁用连接切换
                        }
                    )
                    
                    // 搜索和筛选
                    SearchAndFilterSection(
                        searchQuery = searchQuery,
                        selectedDeviceType = selectedDeviceType,
                        onSearchQueryChange = { /* 暂时禁用 */ },
                        onDeviceTypeChange = { /* 暂时禁用 */ }
                    )
                    
                    // 设备统计
                    DeviceStatsCard(devices = devices)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // 操作按钮
                    ActionButtonsSection(
                        selectedDevices = selectedDevices,
                        onRefresh = { 
                            // 简化：暂时禁用刷新
                        },
                        onClearSelection = { /* 暂时禁用 */ }
                    )
                }
            }
            
            // 右侧设备列表 (70%)
            Card(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 设备列表头部
                    DeviceListHeader(
                        deviceCount = devices.size,
                        selectedCount = selectedDevices.size,
                        isLoading = isLoading
                    )
                    
                    Divider()
                    
                    // 设备列表内容
                    if (isLoading && devices.isEmpty()) {
                        LoadingContent()
                    } else if (devices.isEmpty()) {
                        EmptyDeviceContent()
                    } else {
                        DeviceGridContent(
                            devices = devices,
                            selectedDevices = selectedDevices,
                            onDeviceClick = { device ->
                                onNavigateToDeviceDetail(device.id)
                            },
                            onDeviceSelect = { /* 暂时禁用 */ }
                        )
                    }
                }
            }
        }
        
        // 错误消息显示
        if (errorMessage.isNotEmpty()) {
            LaunchedEffect(errorMessage) {
                // 显示错误消息
            }
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

// =============================================================================
// 1280*800 横屏优化布局组件
// =============================================================================

/**
 * 连接状态卡片
 */
@Composable
private fun ConnectionStatusCard(
    isConnected: Boolean,
    onToggleConnection: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = null,
                tint = if (isConnected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isConnected) "已连接" else "未连接",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isConnected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onToggleConnection,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isConnected) "断开" else "连接")
            }
        }
    }
}

/**
 * 搜索和筛选区域
 */
@Composable
private fun SearchAndFilterSection(
    searchQuery: String,
    selectedDeviceType: DeviceType?,
    onSearchQueryChange: (String) -> Unit,
    onDeviceTypeChange: (DeviceType?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "搜索和筛选",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("搜索设备") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedDeviceType?.displayName ?: "全部类型",
                onValueChange = {},
                readOnly = true,
                label = { Text("设备类型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("全部类型") },
                    onClick = {
                        onDeviceTypeChange(null)
                        expanded = false
                    }
                )
                
                DeviceType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            onDeviceTypeChange(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 设备统计卡片
 */
@Composable
private fun DeviceStatsCard(devices: List<Device>) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "设备统计",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("总数", style = MaterialTheme.typography.bodyMedium)
                Text("${devices.size}", fontWeight = FontWeight.Medium)
            }
            
            val onlineCount = devices.count { it.isOnline }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("在线", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$onlineCount", 
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            val offlineCount = devices.size - onlineCount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("离线", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$offlineCount", 
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 操作按钮区域
 */
@Composable
private fun ActionButtonsSection(
    selectedDevices: Set<String>,
    onRefresh: () -> Unit,
    onClearSelection: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("刷新列表")
        }
        
        if (selectedDevices.isNotEmpty()) {
            Button(
                onClick = onClearSelection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("清除选择")
            }
        }
    }
}

/**
 * 设备列表头部
 */
@Composable
private fun DeviceListHeader(
    deviceCount: Int,
    selectedCount: Int,
    isLoading: Boolean
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
                text = "设备列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "共 $deviceCount 台设备" + if (selectedCount > 0) " (已选择 $selectedCount 台)" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

/**
 * 加载内容
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("正在加载设备列表...")
        }
    }
}

/**
 * 空设备内容
 */
@Composable
private fun EmptyDeviceContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
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
                text = "请检查网络连接或刷新设备列表",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * 设备网格内容 - 3列网格布局适配1280*800
 */
@Composable
private fun DeviceGridContent(
    devices: List<Device>,
    selectedDevices: Set<String>,
    onDeviceClick: (Device) -> Unit,
    onDeviceSelect: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = devices.chunked(3), // 每行3个设备
            key = { row -> row.first().id }
        ) { deviceRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                deviceRow.forEach { device ->
                    DeviceCard(
                        device = device,
                        isSelected = selectedDevices.contains(device.id),
                        onClick = { onDeviceClick(device) },
                        onSelectionChange = { onDeviceSelect(device.id) }
                    )
                }
                
                // 填充空白位置
                repeat(3 - deviceRow.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
