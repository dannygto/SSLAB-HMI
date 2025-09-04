package com.sslab.hmi.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// AI助手消息数据类
data class AIMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val type: AIMessageType = AIMessageType.TEXT
)

enum class AIMessageType {
    TEXT,
    DEVICE_CONTROL,
    EXPERIMENT_GUIDE,
    SAFETY_ALERT,
    SYSTEM_STATUS
}

// AI助手悬浮按钮
@Composable
fun AIAssistantFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasNewMessage: Boolean = false
) {
    val gradientColors = listOf(
        Color(0xFF1E3A8A), // 深蓝
        Color(0xFF3B82F6), // 中蓝
        Color(0xFF60A5FA)  // 浅蓝
    )
    
    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = CircleShape
                ),
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "AI助手",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // 新消息指示器
        if (hasNewMessage) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Red, CircleShape)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

// AI助手对话界面
@Composable
fun AIAssistantDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    messages: List<AIMessage>,
    onSendMessage: (String) -> Unit,
    onDeviceControl: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AIAssistantContent(
                messages = messages,
                onSendMessage = onSendMessage,
                onDeviceControl = onDeviceControl,
                onClose = onDismiss,
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
            )
        }
    }
}

@Composable
private fun AIAssistantContent(
    messages: List<AIMessage>,
    onSendMessage: (String) -> Unit,
    onDeviceControl: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    
    val gradientColors = listOf(
        Color(0xFF1E3A8A),
        Color(0xFF3B82F6),
        Color(0xFF93C5FD)
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(gradientColors.reversed())
                )
                .padding(16.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SSLAB智能助手",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        onDeviceControl = onDeviceControl
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 输入区域
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("询问AI助手或输入设备控制指令...", color = Color.White.copy(0.7f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E3A8A)
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送"
                    )
                }
            }
            
            // 快捷操作按钮
            QuickActionButtons(
                onDeviceControl = onDeviceControl,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: AIMessage,
    onDeviceControl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 8.dp)
            )
        }
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = if (message.isFromUser) 16.dp else 4.dp,
                topEnd = if (message.isFromUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) 
                    Color.White.copy(0.9f) 
                else 
                    Color.White.copy(0.8f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    color = Color(0xFF1E3A8A),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // 如果是设备控制消息，显示操作按钮
                if (message.type == AIMessageType.DEVICE_CONTROL && !message.isFromUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onDeviceControl(message.content) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("执行操作", color = Color.White)
                    }
                }
            }
        }
        
        if (message.isFromUser) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun QuickActionButtons(
    onDeviceControl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickActions = listOf(
        "打开所有电源" to Icons.Default.Power,
        "检查设备状态" to Icons.Default.DeviceHub,
        "环境监测报告" to Icons.Default.Assessment,
        "安全检查" to Icons.Default.Security
    )
    
    LazyRow(
        modifier = modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quickActions) { (action, icon) ->
            AssistChip(
                onClick = { onDeviceControl(action) },
                label = { Text(action, color = Color.White) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color.White.copy(0.2f)
                )
            )
        }
    }
}

// AI助手状态指示器
@Composable
fun AIStatusIndicator(
    isThinking: Boolean,
    modifier: Modifier = Modifier
) {
    if (isThinking) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "AI助手正在思考...",
                color = Color.White.copy(0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
