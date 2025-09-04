package com.sslab.hmi.ui.screens.power

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachingPowerScreen(
    viewModel: TeachingPowerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val teachingPowerStatus by viewModel.teachingPowerStatus.collectAsState()
    val studentGroups by viewModel.studentGroups.collectAsState()
    val studentDevices by viewModel.studentDevices.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    
    // 显示错误信息
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 可以显示Snackbar或其他错误提示
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "教学电源管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 低压教学电源设置
                LowVoltagePowerCard(
                    modifier = Modifier.weight(1f),
                    isEnabled = teachingPowerStatus?.lowVoltageEnabled ?: false,
                    realTimeVoltage = teachingPowerStatus?.dcVoltage?.toString() ?: "0.0",
                    outputVoltage = teachingPowerStatus?.dcVoltage?.toString() ?: "12.0",
                    realTimeCurrent = teachingPowerStatus?.current?.toString() ?: "0.0",
                    onVoltageChange = { viewModel.setLowVoltageValue(it.toFloatOrNull() ?: 0f) },
                    onToggle = viewModel::toggleLowVoltage
                )
                
                // 高压电源设置
                HighVoltagePowerCard(
                    modifier = Modifier.weight(1f),
                    isEnabled = teachingPowerStatus?.highVoltageEnabled ?: false,
                    voltage = teachingPowerStatus?.acVoltage?.toString() ?: "220",
                    onVoltageChange = { viewModel.setHighVoltageValue(it.toFloatOrNull() ?: 220f) },
                    onToggle = viewModel::toggleHighVoltage
                )
                
                // 学生电源控制
                StudentPowerCard(
                    modifier = Modifier.weight(1f),
                    studentGroups = studentGroups,
                    selectedGroupId = selectedGroupId,
                    studentDevices = studentDevices,
                    onGroupSelect = viewModel::selectGroup,
                    onGroupToggle = viewModel::toggleGroupPower,
                    onDeviceToggle = viewModel::toggleStudentDevice
                )
            }
        }
        
        item {
            // 紧急停止和刷新按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = viewModel::emergencyStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("紧急停止")
                }
                
                Button(
                    onClick = viewModel::refreshAllData,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("刷新数据")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LowVoltagePowerCard(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    realTimeVoltage: String,
    outputVoltage: String,
    realTimeCurrent: String,
    onVoltageChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "低压电源设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 实时电压显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("实时电压")
                Text(
                    text = "${realTimeVoltage}V",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 设置输出电压
            OutlinedTextField(
                value = outputVoltage,
                onValueChange = onVoltageChange,
                label = { Text("设置输出电压") },
                trailingIcon = { Text("V") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            // 实时电流显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("实时电流")
                Text(
                    text = "${realTimeCurrent}A",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 输出控制开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("低压电源")
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }
}

@Composable
fun HighVoltagePowerCard(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    voltage: String,
    onVoltageChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "高压电源设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            // 电压设置
            OutlinedTextField(
                value = voltage,
                onValueChange = onVoltageChange,
                label = { Text("设置电压") },
                trailingIcon = { Text("V") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            // 高压开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("高压电源")
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }
}

@Composable
fun StudentPowerCard(
    modifier: Modifier = Modifier,
    studentGroups: List<com.sslab.hmi.data.model.StudentPowerGroup>,
    selectedGroupId: String,
    studentDevices: List<com.sslab.hmi.data.model.StudentDevice>,
    onGroupSelect: (String) -> Unit,
    onGroupToggle: (String) -> Unit,
    onDeviceToggle: (String) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "学生电源控制",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 学生组选择
            Text("学生组:", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                studentGroups.forEach { group ->
                    FilterChip(
                        onClick = { onGroupSelect(group.groupId) },
                        label = { Text(group.groupName) },
                        selected = selectedGroupId == group.groupId
                    )
                }
            }
            
            // 当前组状态
            val currentGroup = studentGroups.find { it.groupId == selectedGroupId }
            currentGroup?.let { group ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${group.groupName} (${group.enabledCount}/${group.deviceCount})")
                        Text(
                            text = "功率: ${group.totalPower}W",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = group.enabledCount > 0,
                        onCheckedChange = { onGroupToggle(group.groupId) }
                    )
                }
            }
            
            // 学生设备列表（显示前6个）
            if (studentDevices.isNotEmpty()) {
                Text("学生设备:", style = MaterialTheme.typography.labelMedium)
                studentDevices.take(6).chunked(2).forEach { rowDevices ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowDevices.forEach { device ->
                            FilterChip(
                                onClick = { onDeviceToggle(device.deviceId) },
                                label = { Text(device.seatNumber) },
                                selected = device.enabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 填充空位
                        if (rowDevices.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
