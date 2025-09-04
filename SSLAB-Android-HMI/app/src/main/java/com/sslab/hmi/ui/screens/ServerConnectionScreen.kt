package com.sslab.hmi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sslab.hmi.ui.viewmodel.DeviceViewModel

/**
 * 服务器连接设置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConnectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    var serverUrl by remember { mutableStateOf("http://192.168.1.100:8080") }
    var isConnecting by remember { mutableStateOf(false) }
    
    val isConnected by viewModel.isConnected.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("服务器连接设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 连接状态卡片
            ConnectionStatusCard(
                isConnected = isConnected,
                serverUrl = serverUrl
            )
            
            // 服务器配置卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "服务器配置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("服务器地址") },
                        placeholder = { Text("http://192.168.1.100:8080") },
                        leadingIcon = {
                            Icon(Icons.Default.Computer, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // 预设服务器地址
                    Text(
                        text = "预设地址",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    PresetServerAddresses { address ->
                        serverUrl = address
                    }
                    
                    // 连接按钮
                    Button(
                        onClick = {
                            isConnecting = true
                            viewModel.connectToServer(serverUrl)
                            // 模拟连接延迟
                            // 实际连接状态通过viewModel.isConnected监听
                        },
                        enabled = serverUrl.isNotBlank() && !isConnecting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("连接中...")
                        } else {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("连接服务器")
                        }
                    }
                    
                    // 断开连接按钮
                    if (isConnected) {
                        OutlinedButton(
                            onClick = {
                                viewModel.disconnectFromServer()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LinkOff, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("断开连接")
                        }
                    }
                }
            }
            
            // 错误消息显示
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "连接错误",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(
                            onClick = { viewModel.clearErrorMessage() }
                        ) {
                            Text("关闭")
                        }
                    }
                }
            }
            
            // 连接说明
            ConnectionInstructions()
        }
    }
    
    // 监听连接状态变化
    LaunchedEffect(isConnected) {
        if (isConnected) {
            isConnecting = false
        }
    }
}

/**
 * 连接状态卡片
 */
@Composable
private fun ConnectionStatusCard(
    isConnected: Boolean,
    serverUrl: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isConnected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isConnected) "已连接" else "未连接",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isConnected) {
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text(
                        text = "请配置并连接服务器",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * 预设服务器地址
 */
@Composable
private fun PresetServerAddresses(
    onAddressSelected: (String) -> Unit
) {
    val presetAddresses = listOf(
        "http://192.168.1.100:8080",
        "http://192.168.0.100:8080", 
        "http://10.0.0.100:8080",
        "http://localhost:8080"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        presetAddresses.forEach { address ->
            OutlinedButton(
                onClick = { onAddressSelected(address) },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 连接说明
 */
@Composable
private fun ConnectionInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "连接说明",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val instructions = listOf(
                "确保设备模拟器服务器正在运行",
                "检查网络连接，确保设备与服务器在同一网络",
                "服务器默认端口为8080",
                "支持HTTP和HTTPS协议",
                "建议在局域网环境下使用"
            )
            
            instructions.forEach { instruction ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = instruction,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
