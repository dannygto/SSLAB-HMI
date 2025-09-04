package com.cn.sslab.hmi.ui.components.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI助手数据类
 */
data class AIAssistantUiState(
    val isExpanded: Boolean = false,
    val state: AIAssistantState = AIAssistantState.IDLE,
    val lastMessage: String = "",
    val isListening: Boolean = false,
    val conversation: List<ChatMessage> = emptyList()
)

/**
 * 聊天消息数据类
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,           // 文本消息
    DEVICE_COMMAND, // 设备控制指令
    SYSTEM_INFO,    // 系统信息
    ERROR          // 错误消息
}

/**
 * AI助手ViewModel
 * 管理AI助手的状态、对话和设备控制功能
 */
@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    // 这里可以注入设备控制、语音识别等服务
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    /**
     * 切换AI助手展开状态
     */
    fun toggleExpanded() {
        _uiState.value = _uiState.value.copy(
            isExpanded = !_uiState.value.isExpanded
        )
    }

    /**
     * 关闭AI助手
     */
    fun dismissAssistant() {
        _uiState.value = _uiState.value.copy(
            isExpanded = false,
            state = AIAssistantState.IDLE
        )
    }

    /**
     * 处理文本输入
     */
    fun handleTextInput(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // 添加用户消息
            addMessage(text, isFromUser = true)
            
            // 设置AI为思考状态
            _uiState.value = _uiState.value.copy(state = AIAssistantState.THINKING)
            
            // 模拟处理延迟
            delay(1500)
            
            // 分析用户输入并生成回复
            val response = processUserInput(text)
            
            // 设置AI为说话状态
            _uiState.value = _uiState.value.copy(state = AIAssistantState.SPEAKING)
            
            // 添加AI回复
            addMessage(response.content, isFromUser = false, type = response.type)
            
            delay(2000)
            
            // 回到空闲状态
            _uiState.value = _uiState.value.copy(state = AIAssistantState.IDLE)
        }
    }

    /**
     * 处理语音输入
     */
    fun handleVoiceInput() {
        if (_uiState.value.isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    /**
     * 开始语音聆听
     */
    private fun startListening() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isListening = true,
                state = AIAssistantState.LISTENING
            )
            
            // 模拟语音识别过程
            delay(3000)
            
            // 模拟识别到的文本
            val recognizedText = "打开A组电源"
            
            _uiState.value = _uiState.value.copy(isListening = false)
            
            // 处理识别到的文本
            handleTextInput(recognizedText)
        }
    }

    /**
     * 停止语音聆听
     */
    private fun stopListening() {
        _uiState.value = _uiState.value.copy(
            isListening = false,
            state = AIAssistantState.IDLE
        )
    }

    /**
     * 添加消息到对话历史
     */
    private fun addMessage(content: String, isFromUser: Boolean, type: MessageType = MessageType.TEXT) {
        val message = ChatMessage(
            content = content,
            isFromUser = isFromUser,
            type = type
        )
        
        val updatedConversation = _uiState.value.conversation + message
        _uiState.value = _uiState.value.copy(
            conversation = updatedConversation,
            lastMessage = if (!isFromUser) content else _uiState.value.lastMessage
        )
    }

    /**
     * 处理用户输入并生成响应
     */
    private fun processUserInput(input: String): ChatMessage {
        val lowerInput = input.lowercase()
        
        return when {
            // 设备控制指令
            lowerInput.contains("打开") && lowerInput.contains("电源") -> {
                ChatMessage(
                    content = "已为您打开相应电源组，请查看电源管理界面确认状态。",
                    isFromUser = false,
                    type = MessageType.DEVICE_COMMAND
                )
            }
            
            lowerInput.contains("关闭") && lowerInput.contains("电源") -> {
                ChatMessage(
                    content = "已为您关闭相应电源组，所有连接设备已断电。",
                    isFromUser = false,
                    type = MessageType.DEVICE_COMMAND
                )
            }
            
            lowerInput.contains("温度") || lowerInput.contains("湿度") -> {
                ChatMessage(
                    content = "当前实验室温度：23.5°C，湿度：45%。环境条件适宜进行实验。",
                    isFromUser = false,
                    type = MessageType.SYSTEM_INFO
                )
            }
            
            lowerInput.contains("实验") && lowerInput.contains("步骤") -> {
                ChatMessage(
                    content = "实验步骤指导：\n1. 确认设备连接\n2. 检查电源状态\n3. 校准测量设备\n4. 开始数据采集\n如需详细说明，请访问实验指导界面。",
                    isFromUser = false,
                    type = MessageType.SYSTEM_INFO
                )
            }
            
            lowerInput.contains("安全") -> {
                ChatMessage(
                    content = "安全提醒：\n⚠️ 操作前请确认所有安全措施\n⚠️ 戴好防护设备\n⚠️ 检查接地线连接\n⚠️ 确认紧急停止按钮位置",
                    isFromUser = false,
                    type = MessageType.SYSTEM_INFO
                )
            }
            
            lowerInput.contains("故障") || lowerInput.contains("问题") -> {
                ChatMessage(
                    content = "正在检测系统状态...\n✅ 所有设备运行正常\n✅ 网络连接稳定\n✅ 电源供应正常\n如有具体故障现象，请详细描述。",
                    isFromUser = false,
                    type = MessageType.SYSTEM_INFO
                )
            }
            
            lowerInput.contains("帮助") || lowerInput.contains("功能") -> {
                ChatMessage(
                    content = "我可以帮您：\n🔧 控制实验室设备\n📊 查询环境数据\n📋 提供实验指导\n⚠️ 安全操作提醒\n🔍 故障诊断分析\n\n试试说：\"打开A组电源\"、\"当前温度\"、\"实验步骤\"等",
                    isFromUser = false,
                    type = MessageType.SYSTEM_INFO
                )
            }
            
            lowerInput.contains("你好") || lowerInput.contains("您好") -> {
                ChatMessage(
                    content = "您好！我是SSLAB AI助手，很高兴为您服务。\n我可以帮助您控制设备、监控环境、指导实验操作。有什么需要帮助的吗？",
                    isFromUser = false
                )
            }
            
            else -> {
                ChatMessage(
                    content = "抱歉，我暂时无法理解您的指令。\n请尝试使用以下关键词：\n• 设备控制：\"打开/关闭电源\"\n• 环境查询：\"温度\"、\"湿度\"\n• 实验指导：\"实验步骤\"、\"安全提醒\"\n• 系统检查：\"故障检测\"、\"状态查询\"",
                    isFromUser = false,
                    type = MessageType.ERROR
                )
            }
        }
    }

    /**
     * 清空对话历史
     */
    fun clearConversation() {
        _uiState.value = _uiState.value.copy(
            conversation = emptyList(),
            lastMessage = ""
        )
    }

    /**
     * 模拟设备控制指令执行
     */
    private fun executeDeviceCommand(command: String) {
        // 这里可以集成实际的设备控制逻辑
        // 例如：调用DeviceRepository的方法
        viewModelScope.launch {
            try {
                // 模拟命令执行
                delay(1000)
                
                addMessage(
                    "设备命令执行成功！",
                    isFromUser = false,
                    type = MessageType.DEVICE_COMMAND
                )
            } catch (e: Exception) {
                addMessage(
                    "设备命令执行失败：${e.message}",
                    isFromUser = false,
                    type = MessageType.ERROR
                )
            }
        }
    }

    /**
     * 获取系统状态信息
     */
    private fun getSystemInfo(): String {
        // 这里可以集成实际的系统监控逻辑
        return """
            系统状态概览：
            🔗 网络连接：正常
            ⚡ 电源状态：稳定
            🌡️ 环境温度：23.5°C
            💧 环境湿度：45%
            📡 设备连接：8/10在线
            ⏰ 运行时长：2小时15分钟
        """.trimIndent()
    }
}
