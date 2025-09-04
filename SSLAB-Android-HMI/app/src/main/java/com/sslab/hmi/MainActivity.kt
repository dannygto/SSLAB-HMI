package com.sslab.hmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.cn.sslab.hmi.ui.components.ai.AIAssistantViewModel
import com.cn.sslab.hmi.ui.components.ai.FloatingAIAssistant
import com.cn.sslab.hmi.ui.theme.BlueGradientBrushes
import com.cn.sslab.hmi.ui.theme.SSLABBlueGradientTheme
import com.sslab.hmi.ui.navigation.SSLabNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SSLABBlueGradientTheme {
                MainContent()
            }
        }
    }
}

@Composable
private fun MainContent() {
    val aiViewModel: AIAssistantViewModel = hiltViewModel()
    val aiUiState by aiViewModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BlueGradientBrushes.BackgroundVertical)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { innerPadding ->
            SSLabNavigation(
                modifier = Modifier.padding(innerPadding)
            )
        }
        
        // 系统级AI助手悬浮组件
        FloatingAIAssistant(
            isExpanded = aiUiState.isExpanded,
            onExpandChange = { aiViewModel.toggleExpanded() },
            onVoiceClick = { aiViewModel.handleVoiceInput() },
            onTextInput = { aiViewModel.handleTextInput(it) },
            assistantState = aiUiState.state,
            lastMessage = aiUiState.lastMessage,
            onDismiss = { aiViewModel.dismissAssistant() }
        )
    }
}
