package com.sslab.hmi.ui.screens.interactive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InteractiveTeachingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "互动教学管理",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "互动教学功能正在开发中...",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• 在线答题系统\n• 实时统计分析\n• 学习效果评估\n• 排名显示",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
