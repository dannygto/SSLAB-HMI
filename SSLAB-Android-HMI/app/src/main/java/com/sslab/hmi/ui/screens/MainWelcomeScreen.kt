package com.sslab.hmi.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cn.sslab.hmi.ui.theme.BlueGradientBrushes
import com.cn.sslab.hmi.ui.theme.BlueGradientColors

/**
 * 主欢迎屏幕 - 1280*800 横屏布局
 * 采用SSLAB专用蓝色渐变主题设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWelcomeScreen(
    onNavigateToDeviceList: () -> Unit,
    onNavigateToTeachingPower: () -> Unit,
    onNavigateToEnvironment: () -> Unit,
    onNavigateToClassroomConfig: () -> Unit,
    onNavigateToApiTest: () -> Unit,
    onNavigateToServerConnection: () -> Unit
) {
    // 1280*800 横屏布局，应用蓝色渐变背景
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BlueGradientBrushes.BackgroundVertical)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 左侧标题和状态区域 (40%)
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题区域 - 使用蓝色渐变卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = BlueGradientBrushes.PrimaryHorizontal,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SSLAB",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "智能实验室控制系统",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "HMI控制终端",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // 系统状态卡片 - 使用白色背景提高对比度
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 标题区域
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "系统状态",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = BlueGradientColors.Primary
                            )
                            
                            // 状态指示器
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            BlueGradientColors.Green,
                                            CircleShape
                                        )
                                )
                                Text(
                                    text = "全部正常",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BlueGradientColors.Green,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // 分隔线
                        Divider(
                            color = BlueGradientColors.Primary.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                        
                        // 状态项目网格
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatusItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Wifi,
                                    label = "网络连接",
                                    status = "已连接",
                                    statusColor = BlueGradientColors.Green
                                )
                                StatusItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.DeviceHub,
                                    label = "设备发现",
                                    status = "运行中",
                                    statusColor = BlueGradientColors.Green
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatusItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Security,
                                    label = "系统安全",
                                    status = "正常",
                                    statusColor = BlueGradientColors.Green
                                )
                                StatusItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Storage,
                                    label = "数据同步",
                                    status = "同步中",
                                    statusColor = BlueGradientColors.Primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 版本信息 - 使用蓝色渐变背景
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BlueGradientColors.SurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "版本 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BlueGradientColors.PrimaryText
                        )
                        Text(
                            text = "© 2024 SSLAB",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlueGradientColors.TertiaryText
                        )
                    }
                }
            }

            // 右侧功能菜单区域 (60%)
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "功能导航",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = BlueGradientColors.PrimaryText
                )

                // 主要功能区域 - 2x2网格
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 设备管理 - 使用主蓝色渐变
                        FunctionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            title = "设备管理",
                            description = "设备发现、状态监控\n设备列表管理",
                            icon = Icons.Default.Devices,
                            gradientColors = listOf(
                                BlueGradientColors.DeepBlue,
                                BlueGradientColors.MediumBlue
                            ),
                            onClick = onNavigateToDeviceList
                        )

                        // 教学电源 - 使用辅助蓝色渐变
                        FunctionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            title = "教学电源",
                            description = "电源控制管理\n学生分组设置",
                            icon = Icons.Default.Power,
                            gradientColors = listOf(
                                BlueGradientColors.SkyBlue,
                                BlueGradientColors.CyanBlue
                            ),
                            onClick = onNavigateToTeachingPower
                        )

                        // 环境监测 - 使用绿色渐变
                        FunctionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            title = "环境监测",
                            description = "温湿度监控\nCO2/PM2.5检测",
                            icon = Icons.Default.Nature,
                            gradientColors = listOf(
                                BlueGradientColors.TealGreen,
                                BlueGradientColors.Green
                            ),
                            onClick = onNavigateToEnvironment
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 教室配置 - 使用青绿色渐变
                        FunctionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            title = "教室配置",
                            description = "教室布局设置\n设备分组管理",
                            icon = Icons.Default.School,
                            gradientColors = listOf(
                                BlueGradientColors.TealGreen,
                                BlueGradientColors.Green
                            ),
                            onClick = onNavigateToClassroomConfig
                        )

                        // 系统工具 - 使用中性灰蓝色渐变
                        FunctionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            title = "系统工具",
                            description = "API测试工具\n服务器连接",
                            icon = Icons.Default.Settings,
                            gradientColors = listOf(
                                BlueGradientColors.MediumGray,
                                BlueGradientColors.LightGray
                            ),
                            onClick = onNavigateToApiTest
                        )
                    }
                }

                // 底部快捷操作 - 使用蓝色渐变按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToServerConnection,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BlueGradientColors.MediumBlue
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            BlueGradientColors.MediumBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BlueGradientColors.MediumBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("服务器连接", color = BlueGradientColors.MediumBlue)
                    }
                    
                    OutlinedButton(
                        onClick = onNavigateToApiTest,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BlueGradientColors.SkyBlue
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            BlueGradientColors.SkyBlue
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = BlueGradientColors.SkyBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("API测试", color = BlueGradientColors.SkyBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    status: String,
    statusColor: Color
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.Primary.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标区域
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        statusColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            // 标签文本
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = BlueGradientColors.Primary,
                fontWeight = FontWeight.Medium
            )
            
            // 状态文本
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = statusColor
            )
        }
    }
}

@Composable
private fun FunctionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
