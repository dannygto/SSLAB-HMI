package com.sslab.hmi.ui.screens.device

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DeviceDiscoveryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "设备发现与管理",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "设备管理功能正在开发中...",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• 设备自动发现\n• 二维码扫描添加\n• 设备状态监控\n• 固件升级管理",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
