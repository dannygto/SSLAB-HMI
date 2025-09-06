package com.sslab.hmi.ui.screens.interactive

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cn.sslab.hmi.ui.theme.BlueGradientColors

/**
 * 自适应学生座位网格布局 - 支持最多48个座位，可滚动
 */
@Composable
fun CompactStudentSeatsGrid(
    seats: List<StudentSeat>,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 动态计算行数和列数
    val totalSeats = seats.size
    val (rows, cols) = when {
        totalSeats <= 16 -> Pair(listOf("A", "B", "C", "D"), 4)
        totalSeats <= 24 -> Pair(listOf("A", "B", "C", "D", "E", "F"), 4)
        totalSeats <= 32 -> Pair(listOf("A", "B", "C", "D", "E", "F", "G", "H"), 4)
        totalSeats <= 40 -> Pair(listOf("A", "B", "C", "D", "E"), 8)
        else -> Pair(listOf("A", "B", "C", "D", "E", "F"), 8) // 最多48个座位 (6x8)
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.BackgroundPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 标题和状态图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "学生座位布局 (${totalSeats}个座位)",
                    style = MaterialTheme.typography.titleMedium,
                    color = BlueGradientColors.PrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp // 增大标题字体
                )
                
                // 紧凑版状态图例
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SeatStatus.values().forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        when (status) {
                                            SeatStatus.EMPTY -> Color.Gray
                                            SeatStatus.WAITING -> Color.Blue
                                            SeatStatus.CORRECT -> Color.Green
                                            SeatStatus.INCORRECT -> Color.Red
                                            SeatStatus.TIMEOUT -> Color.Yellow
                                        }, CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = when (status) {
                                    SeatStatus.EMPTY -> "空"
                                    SeatStatus.WAITING -> "等"
                                    SeatStatus.CORRECT -> "对"
                                    SeatStatus.INCORRECT -> "错"
                                    SeatStatus.TIMEOUT -> "超"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = BlueGradientColors.SecondaryText,
                                fontSize = 12.sp, // 增大图例字体
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 可滚动的座位网格布局
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(rows) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (totalSeats <= 16) 80.dp else if (totalSeats <= 24) 70.dp else 60.dp), // 动态调整行高
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 行标签 - 自适应尺寸
                        Box(
                            modifier = Modifier
                                .width(if (totalSeats <= 24) 24.dp else 20.dp)
                                .fillMaxHeight()
                                .background(
                                    BlueGradientColors.AccentBlue.copy(alpha = 0.1f),
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = row,
                                style = MaterialTheme.typography.labelMedium,
                                color = BlueGradientColors.AccentBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (totalSeats <= 24) 14.sp else 12.sp
                            )
                        }
                        
                        // 该行的座位
                        (1..cols).forEach { col ->
                            val seatId = "$row$col"
                            val seat = seats.find { it.seatId == seatId }
                            if (seat != null) {
                                AdaptiveStudentSeatCard(
                                    seat = seat,
                                    onSeatClick = onSeatClick,
                                    totalSeats = totalSeats,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            } else {
                                // 空位占位符
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 自适应学生座位卡片 - 根据总座位数调整大小和字体
 */
@Composable
fun AdaptiveStudentSeatCard(
    seat: StudentSeat,
    onSeatClick: (String) -> Unit,
    totalSeats: Int,
    modifier: Modifier = Modifier
) {
    // 根据座位总数动态调整字体大小
    val seatIdFontSize = when {
        totalSeats <= 16 -> 16.sp
        totalSeats <= 24 -> 14.sp
        totalSeats <= 32 -> 12.sp
        else -> 10.sp
    }
    
    val nameFontSize = when {
        totalSeats <= 16 -> 12.sp
        totalSeats <= 24 -> 11.sp
        totalSeats <= 32 -> 10.sp
        else -> 9.sp
    }
    
    val answerFontSize = when {
        totalSeats <= 16 -> 18.sp
        totalSeats <= 24 -> 16.sp
        totalSeats <= 32 -> 14.sp
        else -> 12.sp
    }
    
    Card(
        modifier = modifier
            .clickable { onSeatClick(seat.seatId) },
        colors = CardDefaults.cardColors(
            containerColor = when (seat.status) {
                SeatStatus.EMPTY -> Color.Gray.copy(alpha = 0.1f)
                SeatStatus.WAITING -> Color.Blue.copy(alpha = 0.1f)
                SeatStatus.CORRECT -> Color.Green.copy(alpha = 0.1f)
                SeatStatus.INCORRECT -> Color.Red.copy(alpha = 0.1f)
                SeatStatus.TIMEOUT -> Color.Yellow.copy(alpha = 0.1f)
            }
        ),
        border = BorderStroke(
            1.dp, when (seat.status) {
                SeatStatus.EMPTY -> Color.Gray
                SeatStatus.WAITING -> Color.Blue
                SeatStatus.CORRECT -> Color.Green
                SeatStatus.INCORRECT -> Color.Red
                SeatStatus.TIMEOUT -> Color.Yellow
            }
        ),
        shape = RoundedCornerShape(if (totalSeats <= 24) 8.dp else 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (totalSeats <= 24) 4.dp else 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 座位号
            Text(
                text = seat.seatId,
                style = MaterialTheme.typography.titleMedium,
                color = when (seat.status) {
                    SeatStatus.EMPTY -> Color.Gray
                    SeatStatus.WAITING -> Color.Blue
                    SeatStatus.CORRECT -> Color.Green
                    SeatStatus.INCORRECT -> Color.Red
                    SeatStatus.TIMEOUT -> Color.Yellow
                },
                fontWeight = FontWeight.Bold,
                fontSize = seatIdFontSize
            )
            
            if (seat.studentName != null) {
                // 学生姓名
                Text(
                    text = seat.studentName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlueGradientColors.PrimaryText,
                    fontSize = nameFontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // 答案显示
                if (seat.lastAnswer != null) {
                    Text(
                        text = seat.lastAnswer,
                        style = MaterialTheme.typography.titleLarge,
                        color = when (seat.status) {
                            SeatStatus.CORRECT -> Color.Green
                            SeatStatus.INCORRECT -> Color.Red
                            SeatStatus.TIMEOUT -> Color.Yellow
                            else -> Color.Blue
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = answerFontSize
                    )
                } else {
                    // 状态指示器
                    Box(
                        modifier = Modifier
                            .size(if (totalSeats <= 24) 8.dp else 6.dp)
                            .background(
                                when (seat.status) {
                                    SeatStatus.WAITING -> Color.Blue
                                    else -> Color.Gray
                                }, CircleShape
                            )
                    )
                }
            } else {
                // 空座位显示
                Text(
                    text = "空座位",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = if (totalSeats <= 24) 10.sp else 8.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 学生座位组件 - 保持原有的大尺寸版本用于其他地方
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
            containerColor = when (seat.status) {
                SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.1f)
                SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.1f)
                SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.1f)
                SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f)
                SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.1f)
            }
        ),
        border = BorderStroke(2.dp, when (seat.status) {
            SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray
            SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue
            SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green
            SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red
            SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow
        }),
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
                color = when (seat.status) {
                    SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray
                    SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue
                    SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green
                    SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red
                    SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow
                },
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
                        .background(when (seat.status) {
                            SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray
                            SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue
                            SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green
                            SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red
                            SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow
                        }, CircleShape)
                )
                
                // 答案显示
                if (seat.lastAnswer != null) {
                    Text(
                        text = seat.lastAnswer,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (seat.status) {
                            SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray
                            SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue
                            SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green
                            SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red
                            SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow
                        },
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
 * 48个学生座位网格布局 (A1-H6)
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
            
            // 8x6网格布局
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // A行到H行
                listOf("A", "B", "C", "D", "E", "F", "G", "H").forEach { row ->
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
                        
                        // 该行的6个座位
                        (1..6).forEach { col ->
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
                                .background(when (status) {
                                    SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray
                                    SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue
                                    SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green
                                    SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red
                                    SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow
                                }, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (status) {
                                SeatStatus.EMPTY -> "空座"
                                SeatStatus.WAITING -> "等待"
                                SeatStatus.CORRECT -> "正确"
                                SeatStatus.INCORRECT -> "错误"
                                SeatStatus.TIMEOUT -> "超时"
                            },
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
                style = MaterialTheme.typography.titleLarge,
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp // 增大统计标题字体
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
                        value = "${if (statistics.totalAnswered + statistics.unanswered > 0) (statistics.totalAnswered * 100) / (statistics.totalAnswered + statistics.unanswered) else 0}%",
                        color = BlueGradientColors.AccentBlue
                    )
                    StatisticItem(
                        title = "正确率", 
                        value = "${if (statistics.totalAnswered > 0) (statistics.correctAnswers * 100) / statistics.totalAnswered else 0}%",
                        color = BlueGradientColors.AccentGreen
                    )
                    StatisticItem(
                        title = "已答题",
                        value = "${statistics.totalAnswered}人",
                        color = BlueGradientColors.AccentBlue
                    )
                    StatisticItem(
                        title = "答对",
                        value = "${statistics.correctAnswers}人",
                        color = BlueGradientColors.AccentGreen
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 答题时间统计
                if (statistics.fastestTime != null && statistics.fastestTime > 0) {
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
                                text = "最快答题时间: ${statistics.fastestTime!! / 1000.0}秒",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BlueGradientColors.AccentGreen,
                                fontWeight = FontWeight.Medium
                            )
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
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            ),
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = BlueGradientColors.SecondaryText
        )
    }
}
