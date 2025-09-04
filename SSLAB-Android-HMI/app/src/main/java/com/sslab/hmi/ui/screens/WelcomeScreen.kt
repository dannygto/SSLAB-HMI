package com.sslab.hmi.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sslab.hmi.ui.components.*
import com.sslab.hmi.ui.theme.*
import com.sslab.hmi.viewmodel.AIAssistantViewModel
import kotlinx.coroutines.launch

/**
 * 欢迎引导页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onFinishWelcome: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 }) // 增加到4页，包含AI助手介绍
    val scope = rememberCoroutineScope()
    val aiAssistantViewModel: AIAssistantViewModel = hiltViewModel()
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 页面指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(4) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 12.dp else 8.dp)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) 
                                    TextOnBlue 
                                else 
                                    TextOnBlue.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 页面内容
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomePage1()
                1 -> WelcomePage2()
                2 -> WelcomePage3()
            }
        }
        
            // 导航按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = TextOnBlue
                        )
                    ) {
                        Text("上一步")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                Button(
                    onClick = {
                        if (pagerState.currentPage < 3) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinishWelcome()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextOnBlue,
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(
                        if (pagerState.currentPage < 3) "下一步" else "开始使用",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 欢迎页面1 - 应用介绍
 */
@Composable
private fun WelcomePage1() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = TextOnBlue
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "欢迎使用 SSLAB HMI",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = TextOnBlue
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "SSLAB AI实验室环境控制系统\n" +
                    "智能设备管理 • 实时环境监测 • 教学电源控制",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextOnBlue.copy(alpha = 0.8f)
        )
    }
}

/**
 * 欢迎页面2 - 功能介绍
 */
@Composable
private fun WelcomePage2() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DevicesOther,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = TextOnBlue
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "强大的设备管理",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = TextOnBlue
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val features = listOf(
            "自动发现网络设备" to Icons.Default.Search,
            "实时设备状态监控" to Icons.Default.Monitor,
            "远程设备控制" to Icons.Default.ControlPoint,
            "批量设备操作" to Icons.Default.SelectAll
        )
        
        features.forEach { (feature, icon) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextOnBlue,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextOnBlue.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * 欢迎页面3 - 连接设置
 */
@Composable
private fun WelcomePage3() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "连接设备模拟器",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "首次使用前，请配置设备模拟器服务器地址",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "配置说明",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• 确保设备模拟器服务器正在运行\n" +
                            "• 默认地址：http://192.168.0.145:8080\n" +
                            "• 可在设置中修改服务器地址\n" +
                            "• 支持局域网和本地连接",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextOnBlue.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * AI助手介绍页面
 */
@Composable
private fun WelcomeAIPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // AI助手图标动画
        Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = TextOnBlue
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "SSLAB 智能助手",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = TextOnBlue
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "您的专属AI实验室助手，随时为您提供帮助",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = TextOnBlue.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // AI功能特性
        val aiFeatures = listOf(
            "🔧 智能设备控制" to "语音或文字控制设备，一键操作",
            "📊 实时状态监测" to "主动监控系统状态，及时报告异常",
            "🛡️ 安全智能提醒" to "自动安全检查，预防实验风险",
            "📚 实验操作指导" to "专业实验流程指导，提升操作效率",
            "🌡️ 环境智能分析" to "实时环境数据分析，优化实验条件"
        )
        
        aiFeatures.forEach { (title, description) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TextOnBlue.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextOnBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnBlue.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = TextOnBlue.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "点击右下角AI图标即可开始对话",
                style = MaterialTheme.typography.bodyMedium,
                color = TextOnBlue.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
