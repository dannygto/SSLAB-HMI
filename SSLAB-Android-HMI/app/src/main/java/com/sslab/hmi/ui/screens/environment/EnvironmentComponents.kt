package com.sslab.hmi.ui.screens.environment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cn.sslab.hmi.ui.theme.BlueGradientColors

/**
 * 空气质量等级枚举
 */
enum class AirQualityLevel(val color: Color, val text: String) {
    EXCELLENT(BlueGradientColors.AccentGreen, "优"),
    GOOD(Color(0xFF92C5F5), "良"),
    MODERATE(Color(0xFFFFA726), "轻度污染"),
    POOR(Color(0xFFFF7043), "中度污染"),
    VERY_POOR(Color(0xFFE57373), "重度污染")
}

/**
 * 环境数据卡片组件
 */
@Composable
fun EnvironmentDataCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    level: AirQualityLevel,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.BackgroundPrimary
        ),
        border = BorderStroke(1.dp, level.color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标和状态指示器
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(32.dp),
                    tint = BlueGradientColors.PrimaryText
                )
                // 状态指示器小圆点
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 16.dp, y = (-16).dp)
                        .clip(CircleShape)
                        .background(BlueGradientColors.AccentGreen, CircleShape),
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = BlueGradientColors.SecondaryText,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 数值
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = BlueGradientColors.PrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = BlueGradientColors.SecondaryText,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                )
            }
            
            // 等级
            Text(
                text = level.text,
                style = MaterialTheme.typography.labelSmall,
                color = level.color,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
            
            // 副标题（如果有）
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueGradientColors.SecondaryText,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * 环境概览卡片
 */
@Composable
fun EnvironmentOverviewCard(
    environmentData: EnvironmentData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                text = "环境数据概览",
                style = MaterialTheme.typography.titleMedium,
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 温度
                EnvironmentDataCard(
                    title = "温度",
                    value = String.format("%.1f", environmentData.temperature),
                    unit = "°C",
                    icon = Icons.Default.Thermostat,
                    level = environmentData.getTemperatureLevel(),
                    modifier = Modifier.weight(1f)
                )
                
                // 湿度
                EnvironmentDataCard(
                    title = "湿度",
                    value = String.format("%.1f", environmentData.humidity),
                    unit = "%",
                    icon = Icons.Default.WaterDrop,
                    level = environmentData.getHumidityLevel(),
                    modifier = Modifier.weight(1f)
                )
                
                // CO2
                EnvironmentDataCard(
                    title = "CO2",
                    value = String.format("%.0f", environmentData.co2),
                    unit = "ppm",
                    icon = Icons.Default.CloudQueue,
                    level = environmentData.getCO2Level(),
                    modifier = Modifier.weight(1f)
                )
                
                // PM2.5
                EnvironmentDataCard(
                    title = "PM2.5",
                    value = String.format("%.0f", environmentData.pm25),
                    unit = "μg/m³",
                    icon = Icons.Default.Air,
                    level = environmentData.getPM25Level(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 时间范围选择器
 */
@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.BackgroundPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeRange.values().forEach { range ->
                FilterChip(
                    onClick = { onRangeSelected(range) },
                    label = {
                        Text(
                            text = range.label,
                            fontSize = 12.sp
                        )
                    },
                    selected = selectedRange == range,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlueGradientColors.AccentBlue,
                        selectedLabelColor = Color.White,
                        containerColor = BlueGradientColors.BackgroundPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 简单的折线图组件
 */
@Composable
fun SimpleLineChart(
    title: String,
    data: List<EnvironmentData>,
    getValue: (EnvironmentData) -> Float,
    unit: String,
    color: Color = BlueGradientColors.AccentBlue,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 当前值显示
            if (data.isNotEmpty()) {
                val currentValue = getValue(data.last())
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format("%.1f", currentValue),
                        style = MaterialTheme.typography.headlineMedium,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BlueGradientColors.SecondaryText,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 简单的数据趋势指示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📈 数据趋势图",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlueGradientColors.SecondaryText
                )
            }
            
            // 显示数据点数量
            Text(
                text = "${data.size} 个数据点",
                style = MaterialTheme.typography.labelSmall,
                color = BlueGradientColors.SecondaryText,
                fontSize = 10.sp
            )
        }
    }
}
