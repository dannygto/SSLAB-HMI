package com.sslab.hmi.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "首页", Icons.Default.Home)
    object TeachingPower : Screen("teaching_power", "教学电源管理", Icons.Default.PowerSettingsNew)
    object Environment : Screen("environment", "实验环境管理", Icons.Default.Sensors)
    object InteractiveTeaching : Screen("interactive_teaching", "互动教学管理", Icons.Default.Quiz)
    object Settings : Screen("settings", "系统设置", Icons.Default.Settings)
    object DeviceDiscovery : Screen("device_discovery", "设备发现", Icons.Default.DeviceHub)
    object ClassroomConfig : Screen("classroom_config", "教室配置", Icons.Default.School)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.TeachingPower,
    Screen.Environment,
    Screen.InteractiveTeaching,
    Screen.Settings
)
