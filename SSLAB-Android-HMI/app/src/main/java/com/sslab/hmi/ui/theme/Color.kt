package com.sslab.hmi.ui.theme

import androidx.compose.ui.graphics.Color

// SSLAB蓝色渐变主题配色方案
val PrimaryBlue = Color(0xFF1E3A8A)      // 深蓝色 - 主色调
val SecondaryBlue = Color(0xFF3B82F6)    // 中蓝色 - 次要色调
val LightBlue = Color(0xFF60A5FA)        // 浅蓝色 - 高亮色调
val VeryLightBlue = Color(0xFF93C5FD)    // 极浅蓝 - 背景色调

// 渐变色组合
val BlueGradientStart = Color(0xFF1E3A8A)  // 渐变起始色
val BlueGradientMiddle = Color(0xFF3B82F6) // 渐变中间色
val BlueGradientEnd = Color(0xFF60A5FA)    // 渐变结束色

// 功能性颜色
val SuccessGreen = Color(0xFF10B981)      // 成功状态
val WarningYellow = Color(0xFFF59E0B)     // 警告状态
val ErrorRed = Color(0xFFEF4444)          // 错误状态
val InfoCyan = Color(0xFF06B6D4)          // 信息状态

// 背景色
val BackgroundLight = Color(0xFFF8FAFC)   // 浅色背景
val BackgroundDark = Color(0xFF0F172A)    // 深色背景

// 文本颜色
val TextPrimary = Color(0xFF1E293B)       // 主要文本
val TextSecondary = Color(0xFF64748B)     // 次要文本
val TextOnBlue = Color(0xFFFFFFFF)        // 蓝色背景上的文本

// SSLAB Deep Blue Theme Colors (保持兼容性)
val DarkBlue900 = PrimaryBlue
val DarkBlue800 = Color(0xFF1565C0)
val DarkBlue700 = Color(0xFF1976D2)
val DarkBlue600 = Color(0xFF1E88E5)
val DarkBlue500 = SecondaryBlue

val LightBlue50 = Color(0xFFE3F2FD)
val LightBlue100 = Color(0xFFBBDEFB)
val LightBlue200 = VeryLightBlue
val LightBlue300 = LightBlue

// Accent Colors
val Orange500 = WarningYellow
val Orange600 = Color(0xFFFF8F00)
val Green500 = SuccessGreen
val Red500 = ErrorRed

// Neutral Colors
val Grey50 = Color(0xFFFAFAFA)
val Grey100 = Color(0xFFF5F5F5)
val Grey200 = Color(0xFFEEEEEE)
val Grey300 = Color(0xFFE0E0E0)
val Grey400 = Color(0xFFBDBDBD)
val Grey500 = Color(0xFF9E9E9E)
val Grey600 = Color(0xFF757575)
val Grey700 = Color(0xFF616161)
val Grey800 = Color(0xFF424242)
val Grey900 = Color(0xFF212121)

// Surface Colors
val SurfaceLight = Color(0xFFFFFBFE)
val SurfaceDark = Color(0xFF1C1B1F)

// Error Colors
val ErrorLight = ErrorRed
val ErrorDark = Color(0xFFFFB4AB)
