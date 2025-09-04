package com.sslab.hmi.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sslab.hmi.ui.components.DashboardCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTeachingPower: () -> Unit = {},
    onNavigateToEnvironment: () -> Unit = {},
    onNavigateToInteractiveTeaching: () -> Unit = {},
    onNavigateToDeviceDiscovery: () -> Unit = {},
    onNavigateToClassroomConfig: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Welcome Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SSLAB-AI实验室环境控制系统",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "智能实验室环境监测与设备控制平台",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        item {
            Text(
                text = "快捷功能",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(
            listOf(
                DashboardItem(
                    title = "教学电源管理",
                    description = "教师电源设置、学生分组控制、一键参数同步",
                    icon = Icons.Default.PowerSettingsNew,
                    onClick = onNavigateToTeachingPower
                ),
                DashboardItem(
                    title = "实验环境管理", 
                    description = "温湿度监测、空气质量、灯光窗帘控制",
                    icon = Icons.Default.Sensors,
                    onClick = onNavigateToEnvironment
                ),
                DashboardItem(
                    title = "互动教学管理",
                    description = "在线答题、实时统计、学习效果分析",
                    icon = Icons.Default.Quiz,
                    onClick = onNavigateToInteractiveTeaching
                ),
                DashboardItem(
                    title = "设备发现与管理",
                    description = "扫码添加设备、状态监控、固件升级",
                    icon = Icons.Default.DeviceHub,
                    onClick = onNavigateToDeviceDiscovery
                ),
                DashboardItem(
                    title = "教室配置管理",
                    description = "分组设置、教室信息、设备分配管理",
                    icon = Icons.Default.School,
                    onClick = onNavigateToClassroomConfig
                )
            )
        ) { item ->
            DashboardCard(
                title = item.title,
                description = item.description,
                icon = item.icon,
                onClick = item.onClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            // System Status Overview
            Text(
                text = "系统状态概览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Online Devices
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Wifi,
                            contentDescription = "在线设备",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "8",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "在线设备",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                // Active Students
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "活跃学生",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "24",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "活跃学生",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

data class DashboardItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
