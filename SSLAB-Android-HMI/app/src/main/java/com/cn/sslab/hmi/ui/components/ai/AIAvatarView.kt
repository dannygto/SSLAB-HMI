package com.cn.sslab.hmi.ui.components.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * AI头像表情枚举
 */
enum class AIExpression {
    NORMAL,    // 正常
    HAPPY,     // 开心
    THINKING,  // 思考
    LISTENING, // 聆听
    SPEAKING,  // 说话
    ERROR      // 错误
}

/**
 * AI助手头像视图组件
 * 根据AI状态显示不同的表情和动画效果
 */
@Composable
fun AIAvatarView(
    state: AIAssistantState,
    modifier: Modifier = Modifier,
    expression: AIExpression = getExpressionFromState(state),
    primaryColor: Color = Color(0xFF1565C0),
    secondaryColor: Color = Color(0xFF2196F3),
    backgroundColor: Color = Color.White,
    showAnimation: Boolean = true
) {
    // 头像动画
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val scaleAnimation by animateFloatAsState(
        targetValue = if (state != AIAssistantState.IDLE) 1.1f else 1f,
        animationSpec = animationSpec,
        label = "scaleAnimation"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0.8f))
                ),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor)
                ),
                shape = CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // AI头像主体
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            drawAIFace(
                expression = expression,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                scale = if (showAnimation) scaleAnimation else 1f
            )
        }
        
        // 状态特效覆盖层
        if (showAnimation && state != AIAssistantState.IDLE) {
            AnimatedAvatarEffect(
                state = state,
                modifier = Modifier.fillMaxSize(),
                primaryColor = primaryColor.copy(alpha = 0.3f)
            )
        }
    }
}

/**
 * 根据AI状态获取对应表情
 */
private fun getExpressionFromState(state: AIAssistantState): AIExpression {
    return when (state) {
        AIAssistantState.IDLE -> AIExpression.NORMAL
        AIAssistantState.LISTENING -> AIExpression.LISTENING
        AIAssistantState.THINKING -> AIExpression.THINKING
        AIAssistantState.SPEAKING -> AIExpression.SPEAKING
    }
}

/**
 * 绘制AI头像面部表情
 */
private fun DrawScope.drawAIFace(
    expression: AIExpression,
    primaryColor: Color,
    secondaryColor: Color,
    scale: Float = 1f
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val faceRadius = (size.minDimension / 2f) * scale
    
    // 头像背景圆形
    drawCircle(
        color = primaryColor.copy(alpha = 0.1f),
        radius = faceRadius,
        center = androidx.compose.ui.geometry.Offset(centerX, centerY)
    )
    
    // 眼睛位置
    val eyeY = centerY - faceRadius * 0.2f
    val eyeRadius = faceRadius * 0.08f
    val eyeSpacing = faceRadius * 0.3f
    
    // 左眼
    val leftEyeX = centerX - eyeSpacing
    drawEye(
        center = androidx.compose.ui.geometry.Offset(leftEyeX, eyeY),
        radius = eyeRadius,
        expression = expression,
        color = primaryColor
    )
    
    // 右眼
    val rightEyeX = centerX + eyeSpacing
    drawEye(
        center = androidx.compose.ui.geometry.Offset(rightEyeX, eyeY),
        radius = eyeRadius,
        expression = expression,
        color = primaryColor
    )
    
    // 嘴巴
    val mouthY = centerY + faceRadius * 0.3f
    drawMouth(
        center = androidx.compose.ui.geometry.Offset(centerX, mouthY),
        width = faceRadius * 0.4f,
        expression = expression,
        color = secondaryColor
    )
    
    // 根据表情添加特殊效果
    when (expression) {
        AIExpression.THINKING -> {
            // 思考气泡
            drawThinkingBubbles(
                centerX = centerX + faceRadius * 0.7f,
                centerY = centerY - faceRadius * 0.5f,
                color = primaryColor.copy(alpha = 0.6f)
            )
        }
        AIExpression.LISTENING -> {
            // 声波效果
            drawSoundWaves(
                startX = centerX - faceRadius * 1.2f,
                centerY = centerY,
                color = secondaryColor.copy(alpha = 0.5f)
            )
        }
        AIExpression.SPEAKING -> {
            // 说话气泡
            drawSpeechBubble(
                centerX = centerX + faceRadius * 0.8f,
                centerY = centerY - faceRadius * 0.6f,
                color = primaryColor.copy(alpha = 0.7f)
            )
        }
        else -> {}
    }
}

/**
 * 绘制眼睛
 */
private fun DrawScope.drawEye(
    center: androidx.compose.ui.geometry.Offset,
    radius: Float,
    expression: AIExpression,
    color: Color
) {
    when (expression) {
        AIExpression.HAPPY -> {
            // 弯曲的开心眼睛
            val path = Path().apply {
                addArc(
                    oval = androidx.compose.ui.geometry.Rect(
                        center = center,
                        radius = radius
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f
                )
            }
            drawPath(
                path = path,
                color = color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.3f)
            )
        }
        AIExpression.THINKING -> {
            // 专注的细长眼睛
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(center.x - radius, center.y),
                end = androidx.compose.ui.geometry.Offset(center.x + radius, center.y),
                strokeWidth = radius * 0.4f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        AIExpression.LISTENING -> {
            // 圆形大眼睛，表示专注聆听
            drawCircle(
                color = color,
                radius = radius * 1.2f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.3f)
            )
            drawCircle(
                color = color,
                radius = radius * 0.4f,
                center = center
            )
        }
        else -> {
            // 普通圆形眼睛
            drawCircle(
                color = color,
                radius = radius,
                center = center
            )
        }
    }
}

/**
 * 绘制嘴巴
 */
private fun DrawScope.drawMouth(
    center: androidx.compose.ui.geometry.Offset,
    width: Float,
    expression: AIExpression,
    color: Color
) {
    val strokeWidth = width * 0.1f
    
    when (expression) {
        AIExpression.HAPPY -> {
            // 开心的弧形嘴巴
            val path = Path().apply {
                addArc(
                    oval = androidx.compose.ui.geometry.Rect(
                        center = center,
                        radius = width * 0.5f
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f
                )
            }
            drawPath(
                path = path,
                color = color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
        AIExpression.SPEAKING -> {
            // 说话的椭圆形嘴巴
            drawOval(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(
                    center.x - width * 0.3f,
                    center.y - width * 0.15f
                ),
                size = androidx.compose.ui.geometry.Size(width * 0.6f, width * 0.3f)
            )
        }
        AIExpression.THINKING -> {
            // 思考的小点嘴巴
            drawCircle(
                color = color,
                radius = width * 0.1f,
                center = center
            )
        }
        else -> {
            // 普通直线嘴巴
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(center.x - width * 0.4f, center.y),
                end = androidx.compose.ui.geometry.Offset(center.x + width * 0.4f, center.y),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

/**
 * 绘制思考气泡
 */
private fun DrawScope.drawThinkingBubbles(
    centerX: Float,
    centerY: Float,
    color: Color
) {
    val bubbleSizes = listOf(4f, 6f, 8f)
    val bubbleSpacing = 8f
    
    bubbleSizes.forEachIndexed { index, size ->
        drawCircle(
            color = color,
            radius = size,
            center = androidx.compose.ui.geometry.Offset(
                centerX + index * bubbleSpacing,
                centerY - index * bubbleSpacing
            )
        )
    }
}

/**
 * 绘制声波
 */
private fun DrawScope.drawSoundWaves(
    startX: Float,
    centerY: Float,
    color: Color
) {
    repeat(3) { i ->
        val waveRadius = 15f + i * 8f
        drawArc(
            color = color,
            startAngle = -30f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                startX - waveRadius,
                centerY - waveRadius
            ),
            size = androidx.compose.ui.geometry.Size(waveRadius * 2, waveRadius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )
    }
}

/**
 * 绘制说话气泡
 */
private fun DrawScope.drawSpeechBubble(
    centerX: Float,
    centerY: Float,
    color: Color
) {
    val bubbleSize = 12f
    
    // 主气泡
    drawCircle(
        color = color,
        radius = bubbleSize,
        center = androidx.compose.ui.geometry.Offset(centerX, centerY)
    )
    
    // 气泡尾巴
    val path = Path().apply {
        moveTo(centerX - bubbleSize * 0.5f, centerY + bubbleSize * 0.5f)
        lineTo(centerX - bubbleSize * 1.2f, centerY + bubbleSize * 1.5f)
        lineTo(centerX - bubbleSize * 0.2f, centerY + bubbleSize * 0.8f)
        close()
    }
    drawPath(path = path, color = color)
}

/**
 * 动画特效覆盖层
 */
@Composable
private fun AnimatedAvatarEffect(
    state: AIAssistantState,
    modifier: Modifier = Modifier,
    primaryColor: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarEffect")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "effectAlpha"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(modifier = modifier) {
        when (state) {
            AIAssistantState.LISTENING -> {
                drawCircle(
                    color = primaryColor.copy(alpha = alpha),
                    radius = size.minDimension / 2f,
                    center = Offset(size.width / 2f, size.height / 2f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
            AIAssistantState.THINKING -> {
                // 旋转光环效果
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                rotate(rotation, centerOffset) {
                    repeat(4) { i ->
                        val angle = i * 90f
                        val radius = size.minDimension / 3f
                        val x = centerOffset.x + cos(angle * PI / 180f).toFloat() * radius
                        val y = centerOffset.y + sin(angle * PI / 180f).toFloat() * radius
                        
                        drawCircle(
                            color = primaryColor.copy(alpha = alpha),
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }
            AIAssistantState.SPEAKING -> {
                // 脉冲效果
                repeat(3) { i ->
                    val delay = i * 0.3f
                    val pulseRadius = (size.minDimension / 2f) * (1f + alpha + delay)
                    drawCircle(
                        color = primaryColor.copy(alpha = (alpha - delay).coerceAtLeast(0f)),
                        radius = pulseRadius,
                        center = Offset(size.width / 2f, size.height / 2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                    )
                }
            }
            else -> {}
        }
    }
}
