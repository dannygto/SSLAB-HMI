package com.sslab.hmi.ui.screens.environment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cn.sslab.hmi.ui.theme.BlueGradientColors

/**
 * 环境监测主屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentMonitoringScreen(
    onNavigateBack: () -> Unit,
    viewModel: EnvironmentMonitoringViewModel = hiltViewModel()
) {
    val currentData by viewModel.currentEnvironmentData.collectAsState()
    val historyData by viewModel.historyData.collectAsState()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueGradientColors.BackgroundPrimary)
            .padding(16.dp)
    ) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = "环境监测",
                    color = BlueGradientColors.PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = BlueGradientColors.PrimaryText
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.refreshData() },
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = BlueGradientColors.AccentBlue
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = BlueGradientColors.PrimaryText
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BlueGradientColors.BackgroundPrimary
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 环境数据概览卡片
            item {
                EnvironmentOverviewCard(
                    environmentData = currentData,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 时间范围选择器
            item {
                TimeRangeSelector(
                    selectedRange = selectedTimeRange,
                    onRangeSelected = viewModel::setTimeRange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 历史数据图表标题
            item {
                Text(
                    text = "历史数据趋势",
                    style = MaterialTheme.typography.titleMedium,
                    color = BlueGradientColors.PrimaryText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 温度趋势图
            item {
                SimpleLineChart(
                    title = "温度变化",
                    data = historyData,
                    getValue = { it.temperature },
                    unit = "°C",
                    color = BlueGradientColors.AccentBlue,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 湿度趋势图
            item {
                SimpleLineChart(
                    title = "湿度变化",
                    data = historyData,
                    getValue = { it.humidity },
                    unit = "%",
                    color = BlueGradientColors.AccentGreen,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // CO2趋势图
            item {
                SimpleLineChart(
                    title = "CO2浓度",
                    data = historyData,
                    getValue = { it.co2 },
                    unit = "ppm",
                    color = Color(0xFFFFA726),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // PM2.5趋势图
            item {
                SimpleLineChart(
                    title = "PM2.5浓度",
                    data = historyData,
                    getValue = { it.pm25 },
                    unit = "μg/m³",
                    color = Color(0xFFFF7043),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 系统状态卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = BlueGradientColors.BackgroundPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "系统状态",
                            style = MaterialTheme.typography.titleMedium,
                            color = BlueGradientColors.PrimaryText,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 传感器状态列表
                        val sensorList = listOf(
                            "温湿度传感器" to true,
                            "CO2传感器" to true,
                            "PM2.5传感器" to true,
                            "数据传输" to true
                        )
                        
                        sensorList.forEach { (sensorName, isOnline) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sensorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BlueGradientColors.PrimaryText
                                )
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(BlueGradientColors.AccentGreen.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(BlueGradientColors.AccentGreen, CircleShape)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(6.dp))
                                    
                                    Text(
                                        text = if (isOnline) "正常" else "离线",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOnline) BlueGradientColors.AccentGreen else Color.Red,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 数据更新时间
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = BlueGradientColors.SecondaryText
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = "最后更新: ${currentData.timestamp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BlueGradientColors.SecondaryText,
                                fontSize = 11.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 设备连接状态指示
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(BlueGradientColors.AccentBlue.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(BlueGradientColors.AccentBlue, CircleShape)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            Text(
                                text = "设备连接正常",
                                style = MaterialTheme.typography.labelSmall,
                                color = BlueGradientColors.AccentBlue,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
