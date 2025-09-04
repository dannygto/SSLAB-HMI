package com.sslab.hmi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sslab.hmi.data.model.Device
import com.sslab.hmi.data.model.DeviceType
import com.sslab.hmi.ui.viewmodel.DeviceViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 设备详情页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceId: String,
    onNavigateBack: () -> Unit,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    // 收集状态
    val devices by viewModel.devices.collectAsState()
    val device = devices.find { it.id == deviceId }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (device == null) {
        // 设备不存在或正在加载
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("加载设备信息...")
            }
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "删除设备")
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 设备基本信息卡片
            DeviceBasicInfoCard(device = device)
            
            // 设备状态卡片
            DeviceStatusCard(
                device = device,
                onPowerToggle = {
                    val command = if (device.isOnline) "power_off" else "power_on"
                    viewModel.sendDeviceCommand(device.id, command)
                }
            )
            
            // 设备属性卡片
            if (device.attributes.isNotEmpty()) {
                DeviceAttributesCard(attributes = device.attributes)
            }
            
            // 设备控制卡片
            DeviceControlCard(
                device = device,
                onSendCommand = { command, params ->
                    viewModel.sendDeviceCommand(device.id, command, params)
                }
            )
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除设备") },
            text = { Text("确定要删除设备 \"${device.name}\" 吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDevice(device.id)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 设备基本信息卡片
 */
@Composable
private fun DeviceBasicInfoCard(device: Device) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "基本信息",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DeviceInfoRow("设备名称", device.name)
            DeviceInfoRow("设备类型", DeviceType.values().find { it.apiValue == device.type }?.displayName ?: "未知设备")
            DeviceInfoRow("设备ID", device.id)
            DeviceInfoRow("IP地址", device.ipAddress)
            DeviceInfoRow("端口", device.port.toString())
            DeviceInfoRow("MAC地址", device.macAddress)
            DeviceInfoRow("固件版本", device.firmwareVersion)
            DeviceInfoRow(
                "最后在线",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(device.lastSeen)
            )
        }
    }
}

/**
 * 设备状态卡片
 */
@Composable
private fun DeviceStatusCard(
    device: Device,
    onPowerToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "设备状态",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = if (device.isOnline) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.outline
                    
                    Icon(
                        imageVector = if (device.isOnline) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = statusColor
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (device.isOnline) "在线" else "离线",
                        style = MaterialTheme.typography.titleMedium,
                        color = statusColor
                    )
                }
                
                Button(
                    onClick = onPowerToggle,
                    enabled = device.isOnline
                ) {
                    Icon(
                        imageVector = if (device.isOnline) Icons.Default.PowerOff else Icons.Default.Power,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (device.isOnline) "关闭电源" else "开启电源")
                }
            }
        }
    }
}

/**
 * 设备属性卡片
 */
@Composable
private fun DeviceAttributesCard(attributes: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "设备参数",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            attributes.forEach { (key, value) ->
                DeviceInfoRow(
                    label = formatAttributeKey(key),
                    value = formatAttributeValue(key, value)
                )
            }
        }
    }
}

/**
 * 设备控制卡片
 */
@Composable
private fun DeviceControlCard(
    device: Device,
    onSendCommand: (String, Map<String, Any>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "设备控制",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 根据设备类型显示不同的控制选项
            val deviceTypeEnum = DeviceType.values().find { it.apiValue == device.type }
            when (deviceTypeEnum) {
                DeviceType.ENVIRONMENT_MONITOR -> {
                    EnvironmentMonitorControls(onSendCommand)
                }
                DeviceType.STUDENT_POWER_TERMINAL -> {
                    PowerTerminalControls(onSendCommand)
                }
                DeviceType.TEACHING_POWER -> {
                    PowerTerminalControls(onSendCommand)
                }
                DeviceType.ENVIRONMENT_CONTROLLER -> {
                    EnvironmentControllerControls(onSendCommand)
                }
                DeviceType.CURTAIN_CONTROLLER -> {
                    CurtainControllerControls(onSendCommand)
                }
                DeviceType.LIGHTING_CONTROLLER -> {
                    LightingControllerControls(onSendCommand)
                }
                DeviceType.LIFT_CONTROL -> {
                    LiftControllerControls(onSendCommand)
                }
                DeviceType.DEVICE_CONTROL -> {
                    Text("设备控制功能")
                }
                else -> {
                    Text("此设备类型暂不支持控制")
                }
            }
        }
    }
}

/**
 * 设备信息行
 */
@Composable
private fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
    }
}

/**
 * 环境监测设备控制
 */
@Composable
private fun EnvironmentMonitorControls(onSendCommand: (String, Map<String, Any>) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onSendCommand("refresh_data", emptyMap()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("刷新数据")
        }
        
        Button(
            onClick = { onSendCommand("calibrate", emptyMap()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Tune, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("校准传感器")
        }
    }
}

/**
 * 电源终端控制
 */
@Composable
private fun PowerTerminalControls(onSendCommand: (String, Map<String, Any>) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSendCommand("power_on", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Power, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("开启")
            }
            
            Button(
                onClick = { onSendCommand("power_off", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PowerOff, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("关闭")
            }
        }
        
        Button(
            onClick = { onSendCommand("reset", emptyMap()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("重置")
        }
    }
}

/**
 * 环境控制器控制
 */
@Composable
private fun EnvironmentControllerControls(onSendCommand: (String, Map<String, Any>) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSendCommand("fan_on", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("风扇开")
            }
            
            Button(
                onClick = { onSendCommand("fan_off", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("风扇关")
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSendCommand("heater_on", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("加热开")
            }
            
            Button(
                onClick = { onSendCommand("heater_off", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("加热关")
            }
        }
    }
}

/**
 * 窗帘控制器控制
 */
@Composable
private fun CurtainControllerControls(onSendCommand: (String, Map<String, Any>) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSendCommand("open", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("打开")
            }
            
            Button(
                onClick = { onSendCommand("close", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("关闭")
            }
        }
        
        Button(
            onClick = { onSendCommand("stop", emptyMap()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("停止")
        }
    }
}

/**
 * 照明控制器控制
 */
@Composable
private fun LightingControllerControls(onSendCommand: (String, Map<String, Any>) -> Unit) {
    var brightness by remember { mutableStateOf(50f) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSendCommand("light_on", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("开灯")
            }
            
            Button(
                onClick = { onSendCommand("light_off", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("关灯")
            }
        }
        
        Text("亮度: ${brightness.toInt()}%")
        Slider(
            value = brightness,
            onValueChange = { brightness = it },
            valueRange = 0f..100f,
            onValueChangeFinished = {
                onSendCommand("set_brightness", mapOf("brightness" to brightness.toInt()))
            }
        )
    }
}

/**
 * 升降控制器控制
 */
@Composable
private fun LiftControllerControls(onSendCommand: (String, Map<String, Any>) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onSendCommand("lift_up", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                Text("上升")
            }
            
            Button(
                onClick = { onSendCommand("lift_down", emptyMap()) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                Text("下降")
            }
        }
        
        Button(
            onClick = { onSendCommand("lift_stop", emptyMap()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("停止")
        }
    }
}

/**
 * 格式化属性键名
 */
private fun formatAttributeKey(key: String): String {
    return when (key) {
        "temperature" -> "温度"
        "humidity" -> "湿度"
        "voltage" -> "电压"
        "current" -> "电流"
        "power" -> "功率"
        "lightLevel" -> "光照强度"
        "airQuality" -> "空气质量"
        "position" -> "位置"
        "brightness" -> "亮度"
        "fanSpeed" -> "风扇转速"
        "isHeating" -> "加热状态"
        "curtainPosition" -> "窗帘位置"
        "liftPosition" -> "升降位置"
        else -> key
    }
}

/**
 * 格式化属性值
 */
private fun formatAttributeValue(key: String, value: Any): String {
    return when {
        key.contains("temperature") -> "${value}°C"
        key.contains("humidity") -> "${value}%"
        key.contains("voltage") -> "${value}V"
        key.contains("current") -> "${value}A"
        key.contains("power") -> "${value}W"
        key.contains("brightness") -> "${value}%"
        key.contains("position") -> "${value}%"
        value is Boolean -> if (value) "开启" else "关闭"
        else -> value.toString()
    }
}
