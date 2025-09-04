package com.sslab.hmi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sslab.hmi.ui.components.AIMessage
import com.sslab.hmi.ui.components.AIMessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIAssistantViewModel @Inject constructor() : ViewModel() {
    
    private val _messages = MutableStateFlow<List<AIMessage>>(emptyList())
    val messages: StateFlow<List<AIMessage>> = _messages.asStateFlow()
    
    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()
    
    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()
    
    private val _hasNewMessage = MutableStateFlow(false)
    val hasNewMessage: StateFlow<Boolean> = _hasNewMessage.asStateFlow()
    
    init {
        // 初始化欢迎消息
        addMessage(
            AIMessage(
                id = "welcome",
                content = "你好！我是SSLAB智能助手，可以帮助您控制设备、监测环境、指导实验和提供安全建议。有什么我可以帮助您的吗？",
                isFromUser = false,
                type = AIMessageType.TEXT
            )
        )
    }
    
    fun showAssistant() {
        _isVisible.value = true
        _hasNewMessage.value = false
    }
    
    fun hideAssistant() {
        _isVisible.value = false
    }
    
    fun sendMessage(content: String) {
        viewModelScope.launch {
            // 添加用户消息
            addMessage(
                AIMessage(
                    id = generateId(),
                    content = content,
                    isFromUser = true
                )
            )
            
            // 模拟AI思考过程
            _isThinking.value = true
            delay(1500)
            _isThinking.value = false
            
            // 生成AI响应
            val response = generateAIResponse(content)
            addMessage(response)
            
            // 如果对话框未显示，标记有新消息
            if (!_isVisible.value) {
                _hasNewMessage.value = true
            }
        }
    }
    
    private fun addMessage(message: AIMessage) {
        _messages.value = _messages.value + message
    }
    
    private fun generateAIResponse(userInput: String): AIMessage {
        val lowercaseInput = userInput.lowercase()
        
        return when {
            // 设备控制相关
            lowercaseInput.contains("打开") || lowercaseInput.contains("启动") -> {
                if (lowercaseInput.contains("电源") || lowercaseInput.contains("power")) {
                    AIMessage(
                        id = generateId(),
                        content = "正在为您打开设备电源。请确认以下操作：\n\n1. 主电源控制器启动\n2. 各模块供电检查\n3. 设备状态监测\n\n是否继续执行？",
                        isFromUser = false,
                        type = AIMessageType.DEVICE_CONTROL
                    )
                } else {
                    AIMessage(
                        id = generateId(),
                        content = "我可以帮您控制以下设备：\n\n• 电源管理系统\n• 环境监测设备\n• 实验仪器\n• 照明系统\n\n请告诉我具体要控制哪个设备。",
                        isFromUser = false,
                        type = AIMessageType.DEVICE_CONTROL
                    )
                }
            }
            
            // 状态查询相关
            lowercaseInput.contains("状态") || lowercaseInput.contains("检查") -> {
                AIMessage(
                    id = generateId(),
                    content = "当前系统状态：\n\n✅ 主电源：正常\n✅ 环境监测：正常\n⚠️  温度传感器：需要校准\n✅ 网络连接：稳定\n\n建议：请检查温度传感器校准状态。",
                    isFromUser = false,
                    type = AIMessageType.SYSTEM_STATUS
                )
            }
            
            // 环境监测相关
            lowercaseInput.contains("环境") || lowercaseInput.contains("温度") || lowercaseInput.contains("湿度") -> {
                AIMessage(
                    id = generateId(),
                    content = "环境监测报告：\n\n🌡️ 温度：22.5°C（正常）\n💧 湿度：45%（适宜）\n🌀 空气质量：良好\n💨 通风状态：正常\n\n所有环境参数均在安全范围内。",
                    isFromUser = false,
                    type = AIMessageType.EXPERIMENT_GUIDE
                )
            }
            
            // 安全相关
            lowercaseInput.contains("安全") || lowercaseInput.contains("警告") -> {
                AIMessage(
                    id = generateId(),
                    content = "🛡️ 安全检查完成：\n\n• 紧急停止按钮：可用\n• 防护设备：就位\n• 通风系统：正常\n• 报警系统：正常\n\n当前安全状态：良好\n\n⚠️ 提醒：实验前请佩戴防护用品",
                    isFromUser = false,
                    type = AIMessageType.SAFETY_ALERT
                )
            }
            
            // 实验指导相关
            lowercaseInput.contains("实验") || lowercaseInput.contains("操作") || lowercaseInput.contains("步骤") -> {
                AIMessage(
                    id = generateId(),
                    content = "📝 实验操作指导：\n\n1️⃣ 准备阶段\n   • 检查设备状态\n   • 准备实验材料\n   • 确认安全措施\n\n2️⃣ 执行阶段\n   • 按照标准流程操作\n   • 实时监控参数\n   • 记录关键数据\n\n3️⃣ 结束阶段\n   • 设备复位\n   • 数据保存\n   • 环境清理\n\n需要详细的某个步骤说明吗？",
                    isFromUser = false,
                    type = AIMessageType.EXPERIMENT_GUIDE
                )
            }
            
            // 帮助信息
            lowercaseInput.contains("帮助") || lowercaseInput.contains("help") -> {
                AIMessage(
                    id = generateId(),
                    content = "🤖 SSLAB智能助手功能：\n\n🔧 设备控制\n• 电源管理\n• 设备启停\n• 参数调节\n\n📊 状态监测\n• 实时状态查询\n• 报警信息\n• 性能分析\n\n🌡️ 环境监测\n• 温湿度监控\n• 空气质量\n• 环境报警\n\n🛡️ 安全管理\n• 安全检查\n• 风险评估\n• 应急响应\n\n📚 实验指导\n• 操作步骤\n• 最佳实践\n• 故障排除\n\n试试说\"打开电源\"或\"检查状态\"！",
                    isFromUser = false,
                    type = AIMessageType.TEXT
                )
            }
            
            // 默认响应
            else -> {
                val responses = listOf(
                    "我理解您的需求。让我为您查询相关信息...",
                    "这是一个很好的问题。根据当前系统状态，我建议...",
                    "我正在分析您的请求。请稍等片刻...",
                    "基于SSLAB系统的最佳实践，我建议您..."
                )
                
                AIMessage(
                    id = generateId(),
                    content = responses.random() + "\n\n您可以询问我关于设备控制、环境监测、实验指导或安全检查的任何问题。\n\n💡 提示：试试说\"帮助\"了解更多功能！",
                    isFromUser = false,
                    type = AIMessageType.TEXT
                )
            }
        }
    }
    
    fun handleDeviceControl(action: String) {
        viewModelScope.launch {
            // 模拟设备控制操作
            _isThinking.value = true
            delay(2000)
            _isThinking.value = false
            
            val response = when {
                action.contains("打开所有电源") -> {
                    AIMessage(
                        id = generateId(),
                        content = "✅ 电源控制操作完成：\n\n• 主电源：已启动\n• 模块1电源：已启动\n• 模块2电源：已启动\n• 模块3电源：已启动\n\n所有设备已成功上电，系统就绪。",
                        isFromUser = false,
                        type = AIMessageType.DEVICE_CONTROL
                    )
                }
                action.contains("检查设备状态") -> {
                    AIMessage(
                        id = generateId(),
                        content = "📊 设备状态检查结果：\n\n🟢 在线设备：12/12\n🟢 电源状态：正常\n🟡 温度状态：略高（建议检查散热）\n🟢 网络连接：稳定\n🟢 存储空间：充足\n\n整体状态：良好",
                        isFromUser = false,
                        type = AIMessageType.SYSTEM_STATUS
                    )
                }
                action.contains("环境监测报告") -> {
                    AIMessage(
                        id = generateId(),
                        content = "🌡️ 环境监测详细报告：\n\n当前读数：\n• 温度：22.8°C ↗️\n• 湿度：43% ↘️\n• 气压：1013.2 hPa\n• CO₂浓度：420 ppm\n\n趋势分析：\n• 温度呈上升趋势\n• 湿度在正常范围\n• 空气质量优良\n\n建议：适当调节空调温度",
                        isFromUser = false,
                        type = AIMessageType.EXPERIMENT_GUIDE
                    )
                }
                action.contains("安全检查") -> {
                    AIMessage(
                        id = generateId(),
                        content = "🛡️ 完整安全检查报告：\n\n✅ 物理安全\n• 门禁系统：正常\n• 监控设备：在线\n• 消防系统：就绪\n\n✅ 设备安全\n• 漏电保护：正常\n• 过载保护：正常\n• 紧急停止：可用\n\n✅ 环境安全\n• 通风系统：正常\n• 有害气体：未检出\n• 辐射水平：正常\n\n🟢 安全等级：A级（安全）",
                        isFromUser = false,
                        type = AIMessageType.SAFETY_ALERT
                    )
                }
                else -> {
                    AIMessage(
                        id = generateId(),
                        content = "操作已执行：$action\n\n请检查设备响应状态。如有异常，请及时联系技术支持。",
                        isFromUser = false,
                        type = AIMessageType.DEVICE_CONTROL
                    )
                }
            }
            
            addMessage(response)
            
            if (!_isVisible.value) {
                _hasNewMessage.value = true
            }
        }
    }
    
    private fun generateId(): String {
        return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
