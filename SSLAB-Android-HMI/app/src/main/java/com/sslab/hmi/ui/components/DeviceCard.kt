package com.sslab.hmi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sslab.hmi.data.model.Device
import com.sslab.hmi.data.model.DeviceType
import java.text.SimpleDateFormat
import java.util.*

/**
 * 设备卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCard(
    device: Device,
    isSelected: Boolean = false,
    onSelectionChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
    onPowerToggle: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 顶部行：设备名称、状态、选择框
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 选择框
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = onSelectionChange
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 设备图标
                    DeviceTypeIcon(
                        deviceType = device.type,
                        isOnline = device.isOnline,
                        modifier = Modifier.size(40.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = device.type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                // 在线状态指示器
                OnlineStatusIndicator(
                    isOnline = device.isOnline,
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                // 更多选项菜单
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (device.isOnline) "关闭电源" else "开启电源") },
                            onClick = {
                                onPowerToggle()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    if (device.isOnline) Icons.Default.PowerOff else Icons.Default.Power,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除设备") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 设备信息行
            DeviceInfoRow(device = device)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 设备属性（如果有的话）
            if (device.attributes.isNotEmpty()) {
                DeviceAttributesSection(attributes = device.attributes)
            }
        }
    }
}

/**
 * 设备类型图标
 */
@Composable
private fun DeviceTypeIcon(
    deviceType: DeviceType,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val icon = when (deviceType) {
        DeviceType.ENVIRONMENT_MONITOR -> Icons.Default.Thermostat
        DeviceType.STUDENT_POWER_TERMINAL -> Icons.Default.PowerSettingsNew
        DeviceType.ENVIRONMENT_CONTROLLER -> Icons.Default.AcUnit
        DeviceType.CURTAIN_CONTROLLER -> Icons.Default.Blinds
        DeviceType.LIGHTING_CONTROLLER -> Icons.Default.Lightbulb
        DeviceType.LIFT_CONTROLLER -> Icons.Default.Elevator
    }
    
    val backgroundColor = if (isOnline) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.outline
        
    val iconColor = if (isOnline) 
        MaterialTheme.colorScheme.onPrimary 
    else 
        MaterialTheme.colorScheme.surface
    
    Box(
        modifier = modifier
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = deviceType.displayName,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 在线状态指示器
 */
@Composable
private fun OnlineStatusIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isOnline) 
        MaterialTheme.colorScheme.primary 
    else 
        MaterialTheme.colorScheme.outline
    
    Box(
        modifier = modifier
            .size(12.dp)
            .background(color, CircleShape)
    )
}

/**
 * 设备信息行
 */
@Composable
private fun DeviceInfoRow(device: Device) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "IP地址",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = device.ipAddress,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "MAC地址",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = device.macAddress.take(12), // 显示前12个字符
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "最后在线",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = formatLastSeen(device.lastSeen),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * 设备属性部分
 */
@Composable
private fun DeviceAttributesSection(attributes: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "设备状态",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 显示关键属性
            attributes.entries.take(3).forEach { (key, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatAttributeKey(key),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = formatAttributeValue(value),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * 格式化最后在线时间
 */
private fun formatLastSeen(lastSeen: Date): String {
    val now = Date()
    val diffInMillis = now.time - lastSeen.time
    
    return when {
        diffInMillis < 60 * 1000 -> "刚刚"
        diffInMillis < 60 * 60 * 1000 -> "${diffInMillis / (60 * 1000)}分钟前"
        diffInMillis < 24 * 60 * 60 * 1000 -> "${diffInMillis / (60 * 60 * 1000)}小时前"
        else -> SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(lastSeen)
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
        "lightLevel" -> "光照"
        "airQuality" -> "空气质量"
        "position" -> "位置"
        "brightness" -> "亮度"
        else -> key
    }
}

/**
 * 格式化属性值
 */
private fun formatAttributeValue(value: Any): String {
    return when (value) {
        is Number -> {
            when {
                value.toString().contains("temperature") -> "${value}°C"
                value.toString().contains("humidity") -> "${value}%"
                value.toString().contains("voltage") -> "${value}V"
                value.toString().contains("current") -> "${value}A"
                value.toString().contains("power") -> "${value}W"
                else -> value.toString()
            }
        }
        is Boolean -> if (value) "开启" else "关闭"
        else -> value.toString()
    }
}
