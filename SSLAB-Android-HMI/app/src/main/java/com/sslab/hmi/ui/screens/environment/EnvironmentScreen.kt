package com.sslab.hmi.ui.screens.environment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EnvironmentScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "实验环境管理",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "环境监测与控制功能正在开发中...",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• 温湿度监测\n• 空气质量检测\n• 灯光控制\n• 窗帘控制\n• 升降台控制",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
