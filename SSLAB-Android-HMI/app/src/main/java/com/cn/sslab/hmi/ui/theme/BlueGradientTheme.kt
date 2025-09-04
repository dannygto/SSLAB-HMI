package com.cn.sslab.hmi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * SSLAB专用蓝色渐变主题系统
 * 提供完整的颜色配置和渐变效果
 */
object BlueGradientColors {
    // 主色调蓝色渐变系统
    val DeepBlue = Color(0xFF1565C0)        // 深蓝
    val MediumBlue = Color(0xFF1976D2)      // 中蓝  
    val BrightBlue = Color(0xFF2196F3)      // 亮蓝
    
    // 主要颜色属性 (用于UI组件引用)
    val Primary = MediumBlue
    val Secondary = Color(0xFF03A9F4)       // 天蓝
    val Surface = Color(0xFFFFFBFE)         // 白色表面
    val SurfaceVariant = Color(0xFFF3F4F6)  // 浅灰表面
    val OnSurface = Color(0xFF0D47A1)       // 表面上的文字
    
    // 强调色系统
    val AccentGreen = Color(0xFF4CAF50)     // 绿色强调
    val AccentOrange = Color(0xFFFF9800)    // 橙色强调
    val AccentBlue = Color(0xFF03A9F4)      // 蓝色强调
    val AccentRed = Color(0xFFFF5722)       // 红色强调
    val AccentYellow = Color(0xFFFFC107)    // 黄色强调
    
    // 辅助渐变色系
    val SkyBlue = Color(0xFF03A9F4)         // 天蓝
    val CyanBlue = Color(0xFF00BCD4)        // 青蓝
    val TealGreen = Color(0xFF009688)       // 青绿
    
    // 警告状态渐变
    val OrangeRed = Color(0xFFFF5722)       // 橙红
    val Orange = Color(0xFFFF9800)          // 橙色
    val Amber = Color(0xFFFFC107)           // 琥珀
    
    // 成功状态渐变
    val TealDark = Color(0xFF00796B)        // 深青
    val Green = Color(0xFF4CAF50)           // 绿色
    val LightGreen = Color(0xFF8BC34A)      // 浅绿
    
    // 中性灰度渐变
    val DarkGray = Color(0xFF37474F)        // 深灰
    val MediumGray = Color(0xFF607D8B)      // 中灰
    val LightGray = Color(0xFF90A4AE)       // 浅灰
    
    // 文字颜色系统
    val PrimaryText = Color(0xFF0D47A1)     // 主要文字 - 深蓝色
    val SecondaryText = Color(0xFF1565C0)   // 次要文字 - 中蓝色  
    val TertiaryText = Color(0xFF455A64)    // 辅助文字 - 灰蓝色
    val DisabledText = Color(0xFF9E9E9E)    // 禁用文字 - 灰色
    
    // 背景色系
    val BackgroundPrimary = Color(0xFFFFFBFE)           // 主背景 - 白色
    val BackgroundSecondary = Color(0xFFF3F4F6)         // 次背景 - 浅灰
    val BackgroundTertiary = Color(0xFFE3F2FD)          // 蓝色背景
    val BackgroundCard = Color(0xFFFFFFFF)              // 卡片背景
    val BackgroundOverlay = Color(0x80000000)           // 遮罩背景
    
    // 表面色系
    val SurfacePrimary = Color(0xFFFFFBFE)              // 主表面
    val SurfaceCard = Color(0xFFFFFFFF)                 // 卡片表面
    val SurfaceElevated = Color(0xFFF8F9FA)             // 提升表面
    
    // 边框和分割线
    val OutlinePrimary = Color(0xFF607D8B)              // 主边框
    val OutlineSecondary = Color(0xFFCFD8DC)            // 次边框
    val OutlineVariant = Color(0xFFE0E0E0)              // 边框变体
    val Divider = Color(0xFFE0E0E0)                     // 分割线
}

/**
 * 渐变刷子配置
 */
object BlueGradientBrushes {
    // 主要背景渐变 (用于primaryBackground引用)
    val primaryBackground = Brush.verticalGradient(
        colors = listOf(
            BlueGradientColors.BackgroundTertiary.copy(alpha = 0.3f),
            BlueGradientColors.BackgroundPrimary
        )
    )
    
    // 主渐变刷子
    val PrimaryHorizontal = Brush.horizontalGradient(
        colors = listOf(
            BlueGradientColors.DeepBlue,
            BlueGradientColors.MediumBlue,
            BlueGradientColors.BrightBlue
        )
    )
    
    val PrimaryVertical = Brush.verticalGradient(
        colors = listOf(
            BlueGradientColors.DeepBlue,
            BlueGradientColors.MediumBlue,
            BlueGradientColors.BrightBlue
        )
    )
    
    val PrimaryRadial = Brush.radialGradient(
        colors = listOf(
            BlueGradientColors.BrightBlue,
            BlueGradientColors.MediumBlue,
            BlueGradientColors.DeepBlue
        )
    )
    
    // 辅助渐变刷子
    val SecondaryHorizontal = Brush.horizontalGradient(
        colors = listOf(
            BlueGradientColors.SkyBlue,
            BlueGradientColors.CyanBlue,
            BlueGradientColors.TealGreen
        )
    )
    
    val SecondaryVertical = Brush.verticalGradient(
        colors = listOf(
            BlueGradientColors.SkyBlue,
            BlueGradientColors.CyanBlue,
            BlueGradientColors.TealGreen
        )
    )
    
    // 背景渐变刷子
    val BackgroundVertical = Brush.verticalGradient(
        colors = listOf(
            BlueGradientColors.BackgroundTertiary.copy(alpha = 0.3f),
            BlueGradientColors.BackgroundPrimary
        )
    )
    
    val BackgroundRadial = Brush.radialGradient(
        colors = listOf(
            BlueGradientColors.BackgroundTertiary.copy(alpha = 0.2f),
            BlueGradientColors.BackgroundPrimary
        )
    )
    
    // 警告状态渐变
    val WarningGradient = Brush.horizontalGradient(
        colors = listOf(
            BlueGradientColors.OrangeRed,
            BlueGradientColors.Orange,
            BlueGradientColors.Amber
        )
    )
    
    // 成功状态渐变
    val SuccessGradient = Brush.horizontalGradient(
        colors = listOf(
            BlueGradientColors.TealDark,
            BlueGradientColors.Green,
            BlueGradientColors.LightGreen
        )
    )
    
    // 按钮渐变
    val ButtonPrimary = Brush.horizontalGradient(
        colors = listOf(
            BlueGradientColors.MediumBlue,
            BlueGradientColors.BrightBlue
        )
    )
    
    val ButtonSecondary = Brush.horizontalGradient(
        colors = listOf(
            BlueGradientColors.SkyBlue,
            BlueGradientColors.CyanBlue
        )
    )
    
    // 卡片阴影渐变
    val CardShadow = Brush.verticalGradient(
        colors = listOf(
            BlueGradientColors.DeepBlue.copy(alpha = 0.1f),
            Color.Transparent
        )
    )
}

/**
 * 浅色主题配色方案
 */
private val LightColorScheme = lightColorScheme(
    primary = BlueGradientColors.MediumBlue,
    onPrimary = Color.White,
    primaryContainer = BlueGradientColors.BackgroundTertiary,
    onPrimaryContainer = BlueGradientColors.PrimaryText,
    
    secondary = BlueGradientColors.SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = BlueGradientColors.BackgroundTertiary.copy(alpha = 0.5f),
    onSecondaryContainer = BlueGradientColors.SecondaryText,
    
    tertiary = BlueGradientColors.TealGreen,
    onTertiary = Color.White,
    tertiaryContainer = BlueGradientColors.TealGreen.copy(alpha = 0.1f),
    onTertiaryContainer = BlueGradientColors.TealDark,
    
    error = BlueGradientColors.OrangeRed,
    onError = Color.White,
    errorContainer = BlueGradientColors.OrangeRed.copy(alpha = 0.1f),
    onErrorContainer = BlueGradientColors.OrangeRed,
    
    background = BlueGradientColors.BackgroundPrimary,
    onBackground = BlueGradientColors.PrimaryText,
    
    surface = BlueGradientColors.SurfacePrimary,
    onSurface = BlueGradientColors.PrimaryText,
    surfaceVariant = BlueGradientColors.SurfaceVariant,
    onSurfaceVariant = BlueGradientColors.TertiaryText,
    
    outline = BlueGradientColors.OutlinePrimary,
    outlineVariant = BlueGradientColors.OutlineVariant,
    
    scrim = BlueGradientColors.BackgroundOverlay,
    inverseSurface = BlueGradientColors.DarkGray,
    inverseOnSurface = Color.White,
    inversePrimary = BlueGradientColors.BrightBlue
)

/**
 * 深色主题配色方案
 */
private val DarkColorScheme = darkColorScheme(
    primary = BlueGradientColors.BrightBlue,
    onPrimary = BlueGradientColors.DeepBlue,
    primaryContainer = BlueGradientColors.DeepBlue,
    onPrimaryContainer = BlueGradientColors.BrightBlue,
    
    secondary = BlueGradientColors.SkyBlue,
    onSecondary = BlueGradientColors.DeepBlue,
    secondaryContainer = BlueGradientColors.DarkGray,
    onSecondaryContainer = BlueGradientColors.SkyBlue,
    
    tertiary = BlueGradientColors.TealGreen,
    onTertiary = Color.Black,
    tertiaryContainer = BlueGradientColors.TealDark,
    onTertiaryContainer = BlueGradientColors.LightGreen,
    
    error = BlueGradientColors.OrangeRed,
    onError = Color.Black,
    errorContainer = BlueGradientColors.OrangeRed.copy(alpha = 0.3f),
    onErrorContainer = BlueGradientColors.Orange,
    
    background = Color(0xFF121212),
    onBackground = Color.White,
    
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = BlueGradientColors.DarkGray,
    onSurfaceVariant = BlueGradientColors.LightGray,
    
    outline = BlueGradientColors.MediumGray,
    outlineVariant = BlueGradientColors.DarkGray,
    
    scrim = Color.Black.copy(alpha = 0.8f),
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    inversePrimary = BlueGradientColors.MediumBlue
)

/**
 * SSLAB蓝色渐变主题
 */
@Composable
fun SSLABBlueGradientTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
