package com.sslab.hmi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sslab.hmi.ui.components.*
import com.sslab.hmi.ui.theme.*
import com.sslab.hmi.viewmodel.AIAssistantViewModel

@Composable
fun MainScreenWithAI(
    content: @Composable () -> Unit
) {
    val aiAssistantViewModel: AIAssistantViewModel = hiltViewModel()
    val isVisible by aiAssistantViewModel.isVisible.collectAsState()
    val hasNewMessage by aiAssistantViewModel.hasNewMessage.collectAsState()
    val messages by aiAssistantViewModel.messages.collectAsState()
    val isThinking by aiAssistantViewModel.isThinking.collectAsState()
    
    val gradientColors = listOf(
        BlueGradientStart,
        BlueGradientMiddle,
        BlueGradientEnd
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(gradientColors))
    ) {
        // 主要内容
        content()
        
        // AI状态指示器
        if (isThinking && !isVisible) {
            AIStatusIndicator(
                isThinking = isThinking,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
        
        // AI助手悬浮按钮
        AIAssistantFloatingButton(
            onClick = { aiAssistantViewModel.showAssistant() },
            hasNewMessage = hasNewMessage,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
        
        // AI助手对话界面
        AIAssistantDialog(
            isVisible = isVisible,
            onDismiss = { aiAssistantViewModel.hideAssistant() },
            messages = messages,
            onSendMessage = { message ->
                aiAssistantViewModel.sendMessage(message)
            },
            onDeviceControl = { action ->
                aiAssistantViewModel.handleDeviceControl(action)
            }
        )
    }
}
