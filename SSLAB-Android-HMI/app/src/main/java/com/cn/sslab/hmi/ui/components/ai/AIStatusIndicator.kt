package com.cn.sslab.hmi.ui.components.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * AI助手状态指示器
 * 根据不同状态显示不同的动画效果
 */
@Composable
fun AIStatusIndicator(
    state: AIAssistantState,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF1565C0),
    secondaryColor: Color = Color(0xFF2196F3),
    animationDuration: Int = 1500
) {
    when (state) {
        AIAssistantState.LISTENING -> ListeningIndicator(
            modifier = modifier,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            animationDuration = animationDuration
        )
        AIAssistantState.THINKING -> ThinkingIndicator(
            modifier = modifier,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            animationDuration = animationDuration
        )
        AIAssistantState.SPEAKING -> SpeakingIndicator(
            modifier = modifier,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            animationDuration = animationDuration
        )
        AIAssistantState.IDLE -> {
            // 空闲状态不显示指示器
        }
    }
}

/**
 * 聆听状态指示器 - 脉冲圆环效果
 */
@Composable
private fun ListeningIndicator(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    animationDuration: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "listening")
    
    // 脉冲动画
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    
    // 透明度动画
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f
        
        // 绘制多层脉冲圆环
        for (i in 0..2) {
            val delayFactor = i * 0.3f
            val adjustedScale = (pulseScale + delayFactor).coerceAtMost(1f)
            val adjustedAlpha = (pulseAlpha * (1f - delayFactor)).coerceAtLeast(0f)
            
            drawCircle(
                color = primaryColor.copy(alpha = adjustedAlpha),
                radius = maxRadius * adjustedScale,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }
        
        // 中心点
        drawCircle(
            color = secondaryColor,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}

/**
 * 思考状态指示器 - 旋转点阵效果
 */
@Composable
private fun ThinkingIndicator(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    animationDuration: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    
    // 旋转动画
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // 点的闪烁动画
    val dotOpacity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration / 3, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotOpacity"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 4f
        val dotCount = 8
        
        rotate(degrees = rotation, pivot = center) {
            for (i in 0 until dotCount) {
                val angle = (i * 360f / dotCount) * PI / 180f
                val dotX = center.x + cos(angle).toFloat() * radius
                val dotY = center.y + sin(angle).toFloat() * radius
                
                // 每个点的延迟闪烁效果
                val phaseDelay = i * (animationDuration / dotCount)
                val adjustedOpacity = (dotOpacity + sin((rotation + phaseDelay) * PI / 180f).toFloat() * 0.3f)
                    .coerceIn(0.2f, 1f)
                
                drawCircle(
                    color = primaryColor.copy(alpha = adjustedOpacity),
                    radius = 3.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(dotX, dotY)
                )
            }
        }
    }
}

/**
 * 回答状态指示器 - 声波效果
 */
@Composable
private fun SpeakingIndicator(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    secondaryColor: Color,
    animationDuration: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking")
    
    // 声波动画
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f
        val barCount = 12
        val barWidth = 3.dp.toPx()
        val maxBarHeight = maxRadius * 0.8f
        
        for (i in 0 until barCount) {
            val angle = (i * 360f / barCount) * PI / 180f
            val waveOffset = sin(wavePhase + i * 0.5f) * 0.5f + 0.5f
            val barHeight = maxBarHeight * (0.3f + waveOffset * 0.7f)
            
            val startX = center.x + cos(angle).toFloat() * (maxRadius - barHeight / 2)
            val startY = center.y + sin(angle).toFloat() * (maxRadius - barHeight / 2)
            val endX = center.x + cos(angle).toFloat() * (maxRadius + barHeight / 2)
            val endY = center.y + sin(angle).toFloat() * (maxRadius + barHeight / 2)
            
            // 颜色渐变
            val colorIntensity = (0.5f + waveOffset * 0.5f)
            val barColor = androidx.compose.ui.graphics.lerp(secondaryColor, primaryColor, colorIntensity)
            
            drawLine(
                color = barColor,
                start = androidx.compose.ui.geometry.Offset(startX, startY),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 错误状态指示器 - 抖动效果
 */
@Composable
fun ErrorIndicator(
    modifier: Modifier = Modifier,
    errorColor: Color = Color(0xFFE53E3E),
    animationDuration: Int = 800
) {
    val infiniteTransition = rememberInfiniteTransition(label = "error")
    
    // 抖动动画
    val shake by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )
    
    // 闪烁动画
    val blink by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration / 4, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Canvas(
        modifier = modifier.offset(x = shake.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 4f
        
        // 错误图标 - X 形状
        val strokeWidth = 4.dp.toPx()
        val halfSize = radius * 0.6f
        
        drawLine(
            color = errorColor.copy(alpha = blink),
            start = androidx.compose.ui.geometry.Offset(center.x - halfSize, center.y - halfSize),
            end = androidx.compose.ui.geometry.Offset(center.x + halfSize, center.y + halfSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        drawLine(
            color = errorColor.copy(alpha = blink),
            start = androidx.compose.ui.geometry.Offset(center.x + halfSize, center.y - halfSize),
            end = androidx.compose.ui.geometry.Offset(center.x - halfSize, center.y + halfSize),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        
        // 外圈
        drawCircle(
            color = errorColor.copy(alpha = blink * 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
