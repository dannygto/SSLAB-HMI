package com.cn.sslab.hmi.ui.components.input

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.cn.sslab.hmi.ui.theme.BlueGradientColors

/**
 * 浮动透明数字输入弹窗
 * 替换系统键盘，提供更紧凑的数字输入体验
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingNumberInput(
    isVisible: Boolean,
    title: String = "数值输入",
    currentValue: String = "",
    unit: String = "",
    maxLength: Int = 6,
    allowDecimal: Boolean = true,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            FloatingNumberInputContent(
                title = title,
                currentValue = currentValue,
                unit = unit,
                maxLength = maxLength,
                allowDecimal = allowDecimal,
                onValueChange = onValueChange,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun FloatingNumberInputContent(
    title: String,
    currentValue: String,
    unit: String,
    maxLength: Int,
    allowDecimal: Boolean,
    onValueChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputValue by remember(currentValue) { mutableStateOf(currentValue) }
    
    // 数字键盘布局
    val numberKeys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (allowDecimal) "." else "0", "0", "⌫")
    )
    
    // 输入验证函数
    fun addDigit(digit: String) {
        when (digit) {
            "⌫" -> {
                if (inputValue.isNotEmpty()) {
                    inputValue = inputValue.dropLast(1)
                    onValueChange(inputValue)
                }
            }
            "." -> {
                if (allowDecimal && !inputValue.contains(".") && inputValue.isNotEmpty()) {
                    inputValue += digit
                    onValueChange(inputValue)
                }
            }
            else -> {
                if (inputValue.length < maxLength) {
                    inputValue += digit
                    onValueChange(inputValue)
                }
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(min = 280.dp, max = 320.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = BlueGradientColors.Primary.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = BlueGradientColors.Primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 显示区域
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BlueGradientColors.Primary.copy(alpha = 0.05f)
                    ),
                    border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (inputValue.isEmpty()) "0" else inputValue,
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (inputValue.isEmpty()) 
                                BlueGradientColors.Primary.copy(alpha = 0.5f) 
                            else BlueGradientColors.Primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (unit.isNotEmpty()) {
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodyLarge,
                                color = BlueGradientColors.Primary.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 数字键盘
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    numberKeys.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                NumberKeyButton(
                                    text = key,
                                    onClick = { addDigit(key) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BlueGradientColors.Primary
                        ),
                        border = BorderStroke(1.dp, BlueGradientColors.Primary.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("取消")
                    }
                    
                    // 确定按钮
                    Button(
                        onClick = { onConfirm(inputValue) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueGradientColors.Primary,
                            contentColor = Color.White
                        ),
                        enabled = inputValue.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberKeyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeleteKey = text == "⌫"
    
    Card(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDeleteKey) 
                BlueGradientColors.Secondary.copy(alpha = 0.1f)
            else BlueGradientColors.Primary.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isDeleteKey)
                BlueGradientColors.Secondary.copy(alpha = 0.3f)
            else BlueGradientColors.Primary.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isDeleteKey) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "删除",
                    tint = BlueGradientColors.Secondary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = BlueGradientColors.Primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
