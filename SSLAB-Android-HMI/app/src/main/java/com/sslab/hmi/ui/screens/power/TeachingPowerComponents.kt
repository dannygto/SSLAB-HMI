package com.sslab.hmi.ui.screens.power

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sslab.hmi.data.model.StudentGroup
import com.sslab.hmi.data.model.StudentDevice
import com.cn.sslab.hmi.ui.theme.BlueGradientColors
import com.cn.sslab.hmi.ui.components.input.FloatingNumberInput

// 扩展属性 - 适配属性名称差异
val StudentDevice.id: String get() = this.deviceId
val StudentDevice.name: String get() = this.studentName
val StudentDevice.isEnabled: Boolean get() = this.enabled

val StudentGroup.isEnabled: Boolean get() = true // 默认启用

// 学生组和设备数据类 - UI层专用
data class StudentPowerGroup(
    val id: String,
    val name: String,
    val groupId: String,
    val isEnabled: Boolean = false
)

// 无涟漪点击修饰符
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    onClick = onClick
)

/**
 * 低压电源设置面板
 */
@Composable
fun LowVoltagePowerPanel(
    lowVoltageSettings: LowVoltageSettings?,
    onVoltageChange: (String) -> Unit,
    onCurrentChange: (String) -> Unit,
    onEnable: (Boolean) -> Unit,
    onAcModeChange: (Boolean) -> Unit = { },
    onShowVoltageInput: (String, String) -> Unit = { _, _ -> },
    onShowCurrentInput: (String, String) -> Unit = { _, _ -> }
) {
    var voltageInput by remember { mutableStateOf(lowVoltageSettings?.targetVoltage?.toString() ?: "5.0") }
    var currentInput by remember { mutableStateOf(lowVoltageSettings?.targetCurrent?.toString() ?: "1.0") }
    
    // 自定义键盘状态
    var showVoltageKeyboard by remember { mutableStateOf(false) }
    var showCurrentKeyboard by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 面板标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BlueGradientColors.Secondary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "低压电源",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BlueGradientColors.Primary
            )
        }
        
        Divider(color = BlueGradientColors.Primary.copy(alpha = 0.2f))
        
        // 实时状态显示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = BlueGradientColors.SurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "实时状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BlueGradientColors.Primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "输出电压",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlueGradientColors.OnSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${lowVoltageSettings?.currentVoltage ?: "0.0"} V",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BlueGradientColors.AccentGreen
                        )
                    }
                    
                    Column {
                        Text(
                            text = "输出电流",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlueGradientColors.OnSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${lowVoltageSettings?.currentCurrent ?: "0.0"} A",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BlueGradientColors.AccentOrange
                        )
                    }
                }
            }
        }
        
        // 交流/直流模式切换
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "输出模式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = BlueGradientColors.Primary
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 直流按钮
                FilterChip(
                    onClick = { onAcModeChange(false) },
                    label = { Text("直流", style = MaterialTheme.typography.bodySmall) },
                    selected = lowVoltageSettings?.isAcMode == false,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlueGradientColors.AccentGreen,
                        selectedLabelColor = Color.White,
                        labelColor = BlueGradientColors.Primary
                    )
                )
                
                // 交流按钮
                FilterChip(
                    onClick = { onAcModeChange(true) },
                    label = { Text("交流", style = MaterialTheme.typography.bodySmall) },
                    selected = lowVoltageSettings?.isAcMode == true,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlueGradientColors.AccentOrange,
                        selectedLabelColor = Color.White,
                        labelColor = BlueGradientColors.Primary
                    )
                )
            }
        }
        
        // 参数设置
        Text(
            text = "参数设置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BlueGradientColors.Primary
        )
        
        // 电压设置 - 使用自定义键盘
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickableNoRipple { showVoltageKeyboard = true },
            colors = CardDefaults.cardColors(
                containerColor = BlueGradientColors.SurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.3f))
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
                        text = "目标电压",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlueGradientColors.Primary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$voltageInput V",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BlueGradientColors.Primary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = BlueGradientColors.Primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // 电流设置 - 使用自定义键盘
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickableNoRipple { showCurrentKeyboard = true },
            colors = CardDefaults.cardColors(
                containerColor = BlueGradientColors.SurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.3f))
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
                        text = "目标电流",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlueGradientColors.Primary.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$currentInput A",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BlueGradientColors.Primary
                    )
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = BlueGradientColors.Primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 电源开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "电源开关",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = BlueGradientColors.Primary
            )
            
            Switch(
                checked = lowVoltageSettings?.enabled ?: false,
                onCheckedChange = onEnable,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BlueGradientColors.AccentGreen,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = BlueGradientColors.OnSurface.copy(alpha = 0.3f)
                )
            )
        }
        
        // 状态指示器
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (lowVoltageSettings?.enabled == true) 
                            BlueGradientColors.AccentGreen 
                        else 
                            BlueGradientColors.OnSurface.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
            Text(
                text = if (lowVoltageSettings?.enabled == true) "电源已开启" else "电源已关闭",
                style = MaterialTheme.typography.bodyMedium,
                color = BlueGradientColors.OnSurface.copy(alpha = 0.8f)
            )
        }
    }
    
    // 自定义键盘弹窗
    FloatingNumberInput(
        isVisible = showVoltageKeyboard,
        title = "设置目标电压",
        currentValue = voltageInput,
        unit = "V",
        onValueChange = { newValue -> voltageInput = newValue },
        onConfirm = { newValue ->
            voltageInput = newValue
            onVoltageChange(newValue)
            showVoltageKeyboard = false
        },
        onDismiss = { showVoltageKeyboard = false }
    )
    
    FloatingNumberInput(
        isVisible = showCurrentKeyboard,
        title = "设置目标电流",
        currentValue = currentInput,
        unit = "A",
        onValueChange = { newValue -> currentInput = newValue },
        onConfirm = { newValue ->
            currentInput = newValue
            onCurrentChange(newValue)
            showCurrentKeyboard = false
        },
        onDismiss = { showCurrentKeyboard = false }
    )
}

/**
 * 高压电源设置面板 - 重新设计为高压电源和大电流电源两部分
 */
@Composable
fun HighVoltagePowerPanel(
    highVoltageSettings: HighVoltageSettings?,
    highCurrentSettings: HighCurrentSettings?,
    onVoltageChange: (String) -> Unit,
    onCurrentChange: (String) -> Unit,
    onEnable: (Boolean) -> Unit,
    onHighCurrentEnable: (Boolean) -> Unit,
    onVoltageToggle: (Boolean) -> Unit = { } // 240V/300V切换回调
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 高压电源标题（外部）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BlueGradientColors.AccentOrange,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "高压电源",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BlueGradientColors.Primary
            )
        }
        
        Divider(color = BlueGradientColors.Primary.copy(alpha = 0.2f))
        
        // 高压电源部分 - 增加最小高度
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 250.dp)
        ) {
            HighVoltagePowerSection(
                highVoltageSettings = highVoltageSettings,
                onEnable = onEnable,
                onVoltageToggle = onVoltageToggle
            )
        }
        
        // 大电流电源标题（外部）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BlueGradientColors.AccentGreen,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "大电流电源",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BlueGradientColors.Primary
            )
        }
        
        Divider(color = BlueGradientColors.Primary.copy(alpha = 0.2f))
        
        // 大电流电源部分 - 增加最小高度
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 250.dp)
        ) {
            HighCurrentPowerSection(
                highCurrentSettings = highCurrentSettings,
                onEnable = onHighCurrentEnable
            )
        }
    }
}

/**
 * 高压电源区域 - 240V/300V切换 + 输出开关 + 倒计时
 */
@Composable
private fun HighVoltagePowerSection(
    highVoltageSettings: HighVoltageSettings?,
    onEnable: (Boolean) -> Unit,
    onVoltageToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.SurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 240V/300V切换按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "电压档位",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BlueGradientColors.Primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 240V按钮
                    FilterChip(
                        onClick = { onVoltageToggle(false) },
                        label = { Text("240V", style = MaterialTheme.typography.bodySmall) },
                        selected = highVoltageSettings?.is300VMode != true,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlueGradientColors.AccentOrange,
                            selectedLabelColor = Color.White,
                            labelColor = BlueGradientColors.Primary
                        )
                    )
                    
                    // 300V按钮
                    FilterChip(
                        onClick = { onVoltageToggle(true) },
                        label = { Text("300V", style = MaterialTheme.typography.bodySmall) },
                        selected = highVoltageSettings?.is300VMode == true,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlueGradientColors.AccentRed,
                            selectedLabelColor = Color.White,
                            labelColor = BlueGradientColors.Primary
                        )
                    )
                }
            }
            
            // 输出开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "输出开关",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BlueGradientColors.Primary
                )
                
                Switch(
                    checked = highVoltageSettings?.enabled ?: false,
                    onCheckedChange = onEnable,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BlueGradientColors.AccentRed,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = BlueGradientColors.OnSurface.copy(alpha = 0.3f)
                    )
                )
            }
            
            // 倒计时显示
            if (highVoltageSettings?.enabled == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BlueGradientColors.AccentRed.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, BlueGradientColors.AccentRed.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = BlueGradientColors.AccentRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "自动关断倒计时: ${highVoltageSettings.autoShutdownSeconds}秒",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlueGradientColors.AccentRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 大电流电源区域 - 9V40A + 输出开关 + 倒计时
 */
@Composable
private fun HighCurrentPowerSection(
    highCurrentSettings: HighCurrentSettings?,
    onEnable: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.SurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 规格显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "输出规格",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlueGradientColors.Primary.copy(alpha = 0.7f)
                )
                Text(
                    text = "9V / 40A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BlueGradientColors.AccentGreen
                )
            }
            
            // 输出开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "输出开关",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BlueGradientColors.Primary
                )
                
                Switch(
                    checked = highCurrentSettings?.enabled ?: false,
                    onCheckedChange = onEnable,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BlueGradientColors.AccentGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = BlueGradientColors.OnSurface.copy(alpha = 0.3f)
                    )
                )
            }
            
            // 倒计时显示
            if (highCurrentSettings?.enabled == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BlueGradientColors.AccentGreen.copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(1.dp, BlueGradientColors.AccentGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = BlueGradientColors.AccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "自动关断倒计时: ${highCurrentSettings.autoShutdownSeconds}秒",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlueGradientColors.AccentGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 学生电源控制面板
 */
@Composable
fun StudentPowerControlPanel(
    studentGroups: List<StudentGroup>,
    studentDevices: List<StudentDevice>,
    selectedGroupId: String?,
    onGroupSelect: (String) -> Unit,
    onDeviceToggle: (String, Boolean) -> Unit,
    onGroupToggle: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 面板标题
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BlueGradientColors.AccentBlue,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = "学生电源控制",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BlueGradientColors.Primary
            )
        }
        
        Divider(color = BlueGradientColors.Primary.copy(alpha = 0.2f))
        
        // 分组选择
        Text(
            text = "选择分组",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = BlueGradientColors.Primary
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(studentGroups.size) { index ->
                val group = studentGroups[index]
                val isSelected = group.id == selectedGroupId
                val groupDevices = studentDevices.filter { it.groupId == group.id }
                val enabledDevicesCount = groupDevices.count { it.isEnabled }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableNoRipple { onGroupSelect(group.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            BlueGradientColors.Primary.copy(alpha = 0.1f)
                        else 
                            BlueGradientColors.Surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp, 
                        if (isSelected) 
                            BlueGradientColors.Primary 
                        else 
                            BlueGradientColors.Primary.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = BlueGradientColors.Primary
                            )
                            
                            Switch(
                                checked = enabledDevicesCount == groupDevices.size && groupDevices.isNotEmpty(),
                                onCheckedChange = { enabled ->
                                    onGroupToggle(group.id, enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BlueGradientColors.AccentGreen,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = BlueGradientColors.OnSurface.copy(alpha = 0.3f)
                                )
                            )
                        }
                        
                        Text(
                            text = "设备状态: $enabledDevicesCount/${groupDevices.size} 已开启",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlueGradientColors.OnSurface.copy(alpha = 0.7f)
                        )
                        
                        if (isSelected && groupDevices.isNotEmpty()) {
                            Divider(
                                color = BlueGradientColors.Primary.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            
                            // 设备列表
                            groupDevices.forEach { device ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    if (device.isEnabled) 
                                                        BlueGradientColors.AccentGreen 
                                                    else 
                                                        BlueGradientColors.OnSurface.copy(alpha = 0.3f),
                                                    CircleShape
                                                )
                                        )
                                        Text(
                                            text = device.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = BlueGradientColors.OnSurface
                                        )
                                    }
                                    
                                    Switch(
                                        checked = device.isEnabled,
                                        onCheckedChange = { enabled ->
                                            onDeviceToggle(device.id, enabled)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = BlueGradientColors.AccentGreen,
                                            uncheckedThumbColor = Color.White,
                                            uncheckedTrackColor = BlueGradientColors.OnSurface.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
