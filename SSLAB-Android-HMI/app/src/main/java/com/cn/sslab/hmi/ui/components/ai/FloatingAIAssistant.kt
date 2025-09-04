package com.cn.sslab.hmi.ui.components.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * AI助手状态枚举
 */
enum class AIAssistantState {
    IDLE,       // 空闲状态
    LISTENING,  // 聆听中
    THINKING,   // 思考中  
    SPEAKING    // 回答中
}

/**
 * 悬浮AI助手组件
 * 提供系统级AI助手功能，包括语音交互、设备控制、实验指导等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingAIAssistant(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onTextInput: (String) -> Unit = {},
    assistantState: AIAssistantState = AIAssistantState.IDLE,
    lastMessage: String = "",
    onDismiss: () -> Unit = {}
) {
    // 动画状态
    val expandAnimation by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "expandAnimation"
    )

    // 脉冲动画
    val pulseAnimation by animateFloatAsState(
        targetValue = if (assistantState != AIAssistantState.IDLE) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(999f),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 展开的对话界面
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring()
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring()
            )
        ) {
            AIDialogInterface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.7f)
                    .padding(end = 16.dp, bottom = 100.dp),
                onTextInput = onTextInput,
                onVoiceClick = onVoiceClick,
                assistantState = assistantState,
                lastMessage = lastMessage,
                onDismiss = onDismiss
            )
        }

        // 悬浮按钮
        FloatingActionButton(
            onClick = { onExpandChange(!isExpanded) },
            modifier = Modifier
                .padding(16.dp)
                .size((56 * pulseAnimation).dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = BlueGradientTheme.primary,
                    spotColor = BlueGradientTheme.primary
                ),
            shape = CircleShape,
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                BlueGradientTheme.primary,
                                BlueGradientTheme.secondary,
                                BlueGradientTheme.tertiary
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // AI助手图标根据状态变化
                val icon = when (assistantState) {
                    AIAssistantState.IDLE -> Icons.Default.SmartToy
                    AIAssistantState.LISTENING -> Icons.Default.Mic
                    AIAssistantState.THINKING -> Icons.Default.Psychology
                    AIAssistantState.SPEAKING -> Icons.Default.RecordVoiceOver
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "AI助手",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )

                // 状态指示器
                if (assistantState != AIAssistantState.IDLE) {
                    AIStatusIndicator(
                        state = assistantState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * AI对话界面
 */
@Composable
private fun AIDialogInterface(
    modifier: Modifier = Modifier,
    onTextInput: (String) -> Unit,
    onVoiceClick: () -> Unit,
    assistantState: AIAssistantState,
    lastMessage: String,
    onDismiss: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AIAvatarView(
                        state = assistantState,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SSLAB AI助手",
                        style = MaterialTheme.typography.titleMedium,
                        color = BlueGradientTheme.onSurface
                    )
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = BlueGradientTheme.onSurface
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = BlueGradientTheme.outline
            )

            // 消息显示区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = BlueGradientTheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                if (lastMessage.isNotEmpty()) {
                    Text(
                        text = lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BlueGradientTheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "你好！我是SSLAB AI助手，可以帮你控制设备、指导实验、回答问题。试试说\"打开A组电源\"或\"当前温度是多少？\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BlueGradientTheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 输入区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 文字输入框
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { 
                        Text(
                            text = "输入你的问题或指令...",
                            color = BlueGradientTheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueGradientTheme.primary,
                        unfocusedBorderColor = BlueGradientTheme.outline,
                        focusedTextColor = BlueGradientTheme.onSurface,
                        unfocusedTextColor = BlueGradientTheme.onSurface
                    ),
                    singleLine = true
                )

                // 发送按钮
                IconButton(
                    onClick = {
                        if (textInput.isNotEmpty()) {
                            onTextInput(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    BlueGradientTheme.primary,
                                    BlueGradientTheme.secondary
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        tint = Color.White
                    )
                }

                // 语音按钮
                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    BlueGradientTheme.tertiary,
                                    BlueGradientTheme.secondary
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (assistantState == AIAssistantState.LISTENING) 
                            Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "语音输入",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 蓝色渐变主题配置
 */
object BlueGradientTheme {
    val primary = Color(0xFF1565C0)           // 深蓝
    val secondary = Color(0xFF1976D2)         // 中蓝  
    val tertiary = Color(0xFF2196F3)          // 亮蓝
    val surface = Color(0xFFFFFBFE)           // 表面色
    val surfaceVariant = Color(0xFFF3F4F6)    // 表面变体
    val onSurface = Color(0xFF0D47A1)         // 表面上文字
    val onSurfaceVariant = Color(0xFF455A64)  // 表面变体上文字
    val outline = Color(0xFF607D8B)           // 边框色
}
