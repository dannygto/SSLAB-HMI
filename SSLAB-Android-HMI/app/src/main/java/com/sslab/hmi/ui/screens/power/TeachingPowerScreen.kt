package com.sslab.hmi.ui.screens.power

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cn.sslab.hmi.ui.theme.BlueGradientBrushes
import com.cn.sslab.hmi.ui.theme.BlueGradientColors
import com.cn.sslab.hmi.ui.components.input.FloatingNumberInput
import com.sslab.hmi.data.model.StudentPowerGroup

// 扩展属性 - 适配属性名称差异 (需要重新定义以解决包可见性问题)
val com.sslab.hmi.data.model.StudentDevice.displayId: String get() = this.deviceId
val com.sslab.hmi.data.model.StudentDevice.displayName: String get() = this.studentName
val com.sslab.hmi.data.model.StudentDevice.displayEnabled: Boolean get() = this.enabled

/**
 * 教学电源管理界面 - 三栏式布局设计
 * 左栏：低压电源设置
 * 中栏：高压电源设置  
 * 右栏：学生电源控制
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachingPowerScreen(
    viewModel: TeachingPowerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val teachingPowerStatus by viewModel.teachingPowerStatus.collectAsState()
    val lowVoltageSettings by viewModel.lowVoltageSettings.collectAsState()
    val highVoltageSettings by viewModel.highVoltageSettings.collectAsState()
    val highCurrentSettings by viewModel.highCurrentSettings.collectAsState()
    val studentGroups by viewModel.studentGroups.collectAsState()
    val studentDevices by viewModel.studentDevices.collectAsState()
    val selectedGroupId by viewModel.selectedGroupId.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    
    // 自定义数字输入状态
    var showNumberInput by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var inputValue by remember { mutableStateOf("") }
    var inputUnit by remember { mutableStateOf("") }
    var inputCallback by remember { mutableStateOf<(String) -> Unit>({}) }
    
    // 显示错误信息
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 可以显示Snackbar或其他错误提示
        }
    }
    
    // 使用蓝色渐变背景
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueGradientBrushes.primaryBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "教学电源管理",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = BlueGradientColors.Primary
                )
                
                // 一键同步按钮
                ElevatedButton(
                    onClick = { 
                        if (!isSyncing) {
                            viewModel.syncAllStudentDevices()
                        }
                    },
                    enabled = !isSyncing,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (isSyncing) BlueGradientColors.Primary.copy(alpha = 0.6f) else BlueGradientColors.AccentYellow,
                        contentColor = if (isSyncing) Color.White else BlueGradientColors.Primary,
                        disabledContainerColor = BlueGradientColors.Primary.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSyncing) "同步中..." else "一键同步",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 三栏式布局
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 左栏：低压电源设置
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.2f))
                ) {
                    LowVoltagePowerPanel(
                        lowVoltageSettings = lowVoltageSettings,
                        onVoltageChange = { viewModel.updateLowVoltage(it) },
                        onCurrentChange = { viewModel.updateLowCurrent(it) },
                        onEnable = { viewModel.enableLowVoltage(it) },
                        onAcModeChange = { viewModel.setLowVoltageAcMode(it) }
                    )
                }
                
                // 中栏：高压电源设置
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.2f))
                ) {
                    HighVoltagePowerPanel(
                        highVoltageSettings = highVoltageSettings,
                        highCurrentSettings = highCurrentSettings,
                        onVoltageChange = { viewModel.updateHighVoltage(it) },
                        onCurrentChange = { viewModel.updateHighCurrent(it) },
                        onEnable = { viewModel.enableHighVoltage(it) },
                        onHighCurrentEnable = { viewModel.enableHighCurrent(it) },
                        onVoltageToggle = { viewModel.toggleHighVoltageMode(it) }
                    )
                }
                
                // 右栏：学生电源控制
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.2f))
                ) {
                    StudentPowerControlPanel(
                        studentGroups = studentGroups.map { grp: StudentPowerGroup ->
                            com.sslab.hmi.data.model.StudentGroup(
                                id = grp.groupId, 
                                name = grp.groupName, 
                                deviceIds = emptyList()
                            ) 
                        },
                        studentDevices = studentDevices,
                        selectedGroupId = selectedGroupId,
                        onGroupSelect = { viewModel.selectGroup(it) },
                        onDeviceToggle = { deviceId, _ ->
                            viewModel.toggleStudentDevice(deviceId)
                        },
                        onGroupToggle = { groupId, _ ->
                            viewModel.toggleGroupPower(groupId)
                        }
                    )
                }
            }
        }
    }
    
    // 浮动数字输入弹窗
    FloatingNumberInput(
        isVisible = showNumberInput,
        title = inputTitle,
        currentValue = inputValue,
        unit = inputUnit,
        onValueChange = { inputValue = it },
        onConfirm = { value ->
            inputCallback(value)
            showNumberInput = false
            inputValue = ""
        },
        onDismiss = {
            showNumberInput = false
            inputValue = ""
        }
    )
}
