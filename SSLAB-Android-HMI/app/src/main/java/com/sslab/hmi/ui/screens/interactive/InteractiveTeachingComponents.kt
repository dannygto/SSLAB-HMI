package com.sslab.hmi.ui.screens.interactive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cn.sslab.hmi.ui.theme.BlueGradientColors

/**
 * 学生座位组件
 */
@Composable
fun StudentSeatCard(
    seat: StudentSeat,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(80.dp)
            .clickable { onSeatClick(seat.seatId) },
        colors = CardDefaults.cardColors(
            containerColor = seat.status.color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(2.dp, seat.status.color),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 座位号
            Text(
                text = seat.seatId,
                style = MaterialTheme.typography.labelMedium,
                color = seat.status.color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            
            if (seat.studentName != null) {
                Spacer(modifier = Modifier.height(2.dp))
                
                // 学生姓名
                Text(
                    text = seat.studentName,
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueGradientColors.PrimaryText,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // 状态指示器
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(seat.status.color, CircleShape)
                )
                
                // 答案显示
                if (seat.lastAnswer != null) {
                    Text(
                        text = seat.lastAnswer,
                        style = MaterialTheme.typography.labelSmall,
                        color = seat.status.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            } else {
                // 空座位显示
                Icon(
                    imageVector = Icons.Default.EventSeat,
                    contentDescription = "空座位",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 16个学生座位网格布局 (A1-D4)
 */
@Composable
fun StudentSeatsGrid(
    seats: List<StudentSeat>,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.BackgroundPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "学生座位布局",
                style = MaterialTheme.typography.titleMedium,
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 4x4网格布局
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // A行到D行
                listOf("A", "B", "C", "D").forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 行标签
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BlueGradientColors.AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = row,
                                style = MaterialTheme.typography.titleMedium,
                                color = BlueGradientColors.AccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // 该行的4个座位
                        (1..4).forEach { col ->
                            val seatId = "$row$col"
                            val seat = seats.find { it.seatId == seatId }
                            if (seat != null) {
                                StudentSeatCard(
                                    seat = seat,
                                    onSeatClick = onSeatClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 座位状态说明
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SeatStatus.values().toList()) { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(status.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueGradientColors.SecondaryText,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 题目显示卡片
 */
@Composable
fun QuestionCard(
    question: Question?,
    timeRemaining: Int,
    isActive: Boolean,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.BackgroundPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (question != null) "当前题目" else "暂无题目",
                    style = MaterialTheme.typography.titleMedium,
                    color = BlueGradientColors.PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                
                if (question != null && isActive) {
                    // 倒计时显示
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (timeRemaining <= 10) Color.Red.copy(alpha = 0.1f) else BlueGradientColors.AccentBlue.copy(alpha = 0.1f)
                        ),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "倒计时",
                                tint = if (timeRemaining <= 10) Color.Red else BlueGradientColors.AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${timeRemaining}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (timeRemaining <= 10) Color.Red else BlueGradientColors.AccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            if (question != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // 题目难度标签
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = question.difficulty.color.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = question.difficulty.label,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = question.difficulty.color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "时限: ${question.timeLimit}秒",
                        style = MaterialTheme.typography.labelSmall,
                        color = BlueGradientColors.SecondaryText
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 题目内容
                Text(
                    text = question.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BlueGradientColors.PrimaryText,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 选项按钮
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.options.forEachIndexed { index, option ->
                        val optionLetter = ('A' + index).toString()
                        OutlinedButton(
                            onClick = { onOptionSelected(optionLetter) },
                            enabled = isActive,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = BlueGradientColors.BackgroundPrimary,
                                contentColor = BlueGradientColors.PrimaryText
                            ),
                            border = BorderStroke(1.dp, BlueGradientColors.AccentBlue.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 选项字母
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(BlueGradientColors.AccentBlue.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionLetter,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BlueGradientColors.AccentBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // 选项内容
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BlueGradientColors.PrimaryText,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(32.dp))
                
                // 无题目状态
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionMark,
                        contentDescription = "无题目",
                        tint = BlueGradientColors.SecondaryText,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "请发布题目开始答题",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BlueGradientColors.SecondaryText
                    )
                }
            }
        }
    }
}

/**
 * 答题统计卡片
 */
@Composable
fun AnswerStatisticsCard(
    statistics: AnswerStatistics?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.BackgroundPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "答题统计",
                style = MaterialTheme.typography.titleMedium,
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Bold
            )
            
            if (statistics != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // 统计数据网格
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem(
                        title = "参与率",
                        value = "${(statistics.participationRate * 100).toInt()}%",
                        color = BlueGradientColors.AccentBlue
                    )
                    StatisticItem(
                        title = "正确率", 
                        value = "${(statistics.correctRate * 100).toInt()}%",
                        color = BlueGradientColors.AccentGreen
                    )
                    StatisticItem(
                        title = "已答题",
                        value = "${statistics.answeredCount}人",
                        color = BlueGradientColors.AccentBlue
                    )
                    StatisticItem(
                        title = "答对",
                        value = "${statistics.correctCount}人",
                        color = BlueGradientColors.AccentGreen
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 最快答题学生
                if (statistics.fastestStudent != null && statistics.fastestTime != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = BlueGradientColors.AccentGreen.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "最快",
                                tint = BlueGradientColors.AccentGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "最快答题: ${statistics.fastestStudent} (${statistics.fastestTime / 1000.0}秒)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BlueGradientColors.AccentGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // 选项统计
                if (statistics.optionStatistics.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "选项分布",
                        style = MaterialTheme.typography.labelLarge,
                        color = BlueGradientColors.PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statistics.optionStatistics.forEach { (option, count) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = BlueGradientColors.AccentBlue.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BlueGradientColors.AccentBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${count}人",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BlueGradientColors.SecondaryText
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "暂无答题数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlueGradientColors.SecondaryText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 统计项组件
 */
@Composable
fun StatisticItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = BlueGradientColors.SecondaryText,
            fontSize = 10.sp
        )
    }
}
