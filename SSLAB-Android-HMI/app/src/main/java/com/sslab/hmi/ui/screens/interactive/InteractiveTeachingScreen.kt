package com.sslab.hmi.ui.screens.interactive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cn.sslab.hmi.ui.theme.BlueGradientColors

/**
 * 互动教学主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveTeachingScreen(
    viewModel: InteractiveTeachingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val timeRemaining by viewModel.timeRemaining.collectAsStateWithLifecycle()
    
    var showQuestionDialog by remember { mutableStateOf(false) }
    var showSeatInfoDialog by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "互动教学",
                        color = BlueGradientColors.PrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = BlueGradientColors.PrimaryText
                        )
                    }
                },
                actions = {
                    // 刷新按钮
                    IconButton(
                        onClick = { viewModel.refreshData() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = BlueGradientColors.PrimaryText
                        )
                    }
                    
                    // 发布题目按钮
                    IconButton(
                        onClick = { showQuestionDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "发布题目",
                            tint = BlueGradientColors.PrimaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlueGradientColors.BackgroundPrimary
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 停止答题按钮
                if (uiState.currentQuestion != null && uiState.isQuestionActive) {
                    FloatingActionButton(
                        onClick = { viewModel.stopQuestion() },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "停止答题"
                        )
                    }
                }
                
                // 清除答案按钮
                if (statistics != null && statistics!!.answeredCount > 0) {
                    FloatingActionButton(
                        onClick = { viewModel.clearAnswers() },
                        containerColor = BlueGradientColors.AccentBlue
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除答案",
                            tint = BlueGradientColors.BackgroundPrimary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 当前题目卡片
            item {
                QuestionCard(
                    question = uiState.currentQuestion,
                    timeRemaining = timeRemaining,
                    isActive = uiState.isQuestionActive,
                    onOptionSelected = { /* 教师界面不需要选择选项 */ }
                )
            }
            
            // 答题统计卡片
            item {
                AnswerStatisticsCard(
                    statistics = statistics
                )
            }
            
            // 学生座位网格
            item {
                StudentSeatsGrid(
                    seats = uiState.students,
                    onSeatClick = { seatId ->
                        showSeatInfoDialog = seatId
                    }
                )
            }
            
            // 操作控制面板
            item {
                TeacherControlPanel(
                    currentQuestion = uiState.currentQuestion,
                    isQuestionActive = uiState.isQuestionActive,
                    onStartQuestion = { viewModel.startQuestion() },
                    onStopQuestion = { viewModel.stopQuestion() },
                    onClearAnswers = { viewModel.clearAnswers() },
                    onPublishQuestion = { showQuestionDialog = true }
                )
            }
        }
    }
    
    // 发布题目对话框
    if (showQuestionDialog) {
        PublishQuestionDialog(
            onDismiss = { showQuestionDialog = false },
            onPublish = { question ->
                viewModel.publishQuestion(question)
                showQuestionDialog = false
            }
        )
    }
    
    // 座位信息对话框
    showSeatInfoDialog?.let { seatId ->
        SeatInfoDialog(
            seat = uiState.students.find { it.seatId == seatId },
            onDismiss = { showSeatInfoDialog = null },
            onAssignStudent = { studentName ->
                viewModel.assignStudentToSeat(seatId, studentName)
                showSeatInfoDialog = null
            },
            onRemoveStudent = {
                viewModel.removeStudentFromSeat(seatId)
                showSeatInfoDialog = null
            }
        )
    }
}

/**
 * 教师控制面板
 */
@Composable
fun TeacherControlPanel(
    currentQuestion: Question?,
    isQuestionActive: Boolean,
    onStartQuestion: () -> Unit,
    onStopQuestion: () -> Unit,
    onClearAnswers: () -> Unit,
    onPublishQuestion: () -> Unit,
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
                text = "教师控制台",
                style = MaterialTheme.typography.titleMedium,
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 控制按钮网格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 发布题目按钮
                Button(
                    onClick = onPublishQuestion,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGradientColors.AccentBlue,
                        contentColor = BlueGradientColors.BackgroundPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "发布题目"
                        )
                        Text(
                            text = "发布题目",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                
                // 开始/停止答题按钮
                if (currentQuestion != null) {
                    Button(
                        onClick = if (isQuestionActive) onStopQuestion else onStartQuestion,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isQuestionActive) MaterialTheme.colorScheme.error else BlueGradientColors.AccentGreen,
                            contentColor = if (isQuestionActive) MaterialTheme.colorScheme.onError else BlueGradientColors.BackgroundPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (isQuestionActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isQuestionActive) "停止答题" else "开始答题"
                            )
                            Text(
                                text = if (isQuestionActive) "停止答题" else "开始答题",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                
                // 清除答案按钮
                Button(
                    onClick = onClearAnswers,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGradientColors.SecondaryText.copy(alpha = 0.8f),
                        contentColor = BlueGradientColors.BackgroundPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除答案"
                        )
                        Text(
                            text = "清除答案",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 状态指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 题目状态
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentQuestion != null) BlueGradientColors.AccentGreen.copy(alpha = 0.1f) else BlueGradientColors.SecondaryText.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (currentQuestion != null) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = "题目状态",
                            tint = if (currentQuestion != null) BlueGradientColors.AccentGreen else BlueGradientColors.SecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (currentQuestion != null) "有题目" else "无题目",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (currentQuestion != null) BlueGradientColors.AccentGreen else BlueGradientColors.SecondaryText
                        )
                    }
                }
                
                // 答题状态
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isQuestionActive) BlueGradientColors.AccentBlue.copy(alpha = 0.1f) else BlueGradientColors.SecondaryText.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isQuestionActive) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "答题状态",
                            tint = if (isQuestionActive) BlueGradientColors.AccentBlue else BlueGradientColors.SecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isQuestionActive) "答题中" else "已停止",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isQuestionActive) BlueGradientColors.AccentBlue else BlueGradientColors.SecondaryText
                        )
                    }
                }
            }
        }
    }
}

/**
 * 发布题目对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishQuestionDialog(
    onDismiss: () -> Unit,
    onPublish: (Question) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("A") }
    var difficulty by remember { mutableStateOf(QuestionDifficulty.EASY) }
    var timeLimit by remember { mutableStateOf(30) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "发布新题目",
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 题目内容
                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("题目内容") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
                
                // 选项输入
                item {
                    Text(
                        text = "选项设置",
                        style = MaterialTheme.typography.labelLarge,
                        color = BlueGradientColors.PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = optionA,
                        onValueChange = { optionA = it },
                        label = { Text("选项 A") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = optionB,
                        onValueChange = { optionB = it },
                        label = { Text("选项 B") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = optionC,
                        onValueChange = { optionC = it },
                        label = { Text("选项 C") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = optionD,
                        onValueChange = { optionD = it },
                        label = { Text("选项 D") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 正确答案选择
                item {
                    Text(
                        text = "正确答案",
                        style = MaterialTheme.typography.labelLarge,
                        color = BlueGradientColors.PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("A", "B", "C", "D").forEach { option ->
                            FilterChip(
                                selected = correctAnswer == option,
                                onClick = { correctAnswer = option },
                                label = { Text(option) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // 难度设置
                item {
                    Text(
                        text = "题目难度",
                        style = MaterialTheme.typography.labelLarge,
                        color = BlueGradientColors.PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuestionDifficulty.values().forEach { diff ->
                            FilterChip(
                                selected = difficulty == diff,
                                onClick = { difficulty = diff },
                                label = { Text(diff.label) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = diff.color.copy(alpha = 0.2f),
                                    selectedLabelColor = diff.color
                                )
                            )
                        }
                    }
                }
                
                // 时间限制
                item {
                    Text(
                        text = "时间限制: ${timeLimit}秒",
                        style = MaterialTheme.typography.labelLarge,
                        color = BlueGradientColors.PrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Slider(
                        value = timeLimit.toFloat(),
                        onValueChange = { timeLimit = it.toInt() },
                        valueRange = 10f..120f,
                        steps = 21,
                        colors = SliderDefaults.colors(
                            thumbColor = BlueGradientColors.AccentBlue,
                            activeTrackColor = BlueGradientColors.AccentBlue
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank() && 
                        optionC.isNotBlank() && optionD.isNotBlank()) {
                        val question = Question(
                            id = System.currentTimeMillis().toString(),
                            content = content,
                            options = listOf(optionA, optionB, optionC, optionD),
                            correctAnswer = correctAnswer,
                            difficulty = difficulty,
                            timeLimit = timeLimit
                        )
                        onPublish(question)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueGradientColors.AccentBlue,
                    contentColor = BlueGradientColors.BackgroundPrimary
                )
            ) {
                Text("发布")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    color = BlueGradientColors.SecondaryText
                )
            }
        },
        containerColor = BlueGradientColors.BackgroundPrimary
    )
}

/**
 * 座位信息对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatInfoDialog(
    seat: StudentSeat?,
    onDismiss: () -> Unit,
    onAssignStudent: (String) -> Unit,
    onRemoveStudent: () -> Unit
) {
    var studentName by remember { mutableStateOf(seat?.studentName ?: "") }
    
    if (seat != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "座位 ${seat.seatId}",
                    color = BlueGradientColors.PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 当前状态
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = seat.status.color.copy(alpha = 0.1f)
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
                                imageVector = Icons.Default.Person,
                                contentDescription = "学生状态",
                                tint = seat.status.color,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "状态: ${seat.status.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = seat.status.color,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // 学生姓名输入
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("学生姓名") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("请输入学生姓名") }
                    )
                    
                    // 最后答案显示
                    if (seat.lastAnswer != null && seat.lastAnswerTime != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = BlueGradientColors.AccentBlue.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "最后答案: ${seat.lastAnswer}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BlueGradientColors.AccentBlue,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "答题时间: ${seat.lastAnswerTime / 1000.0}秒",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BlueGradientColors.SecondaryText
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (seat.studentName != null) {
                        Button(
                            onClick = onRemoveStudent,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("移除")
                        }
                    }
                    
                    Button(
                        onClick = {
                            if (studentName.isNotBlank()) {
                                onAssignStudent(studentName)
                            }
                        },
                        enabled = studentName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueGradientColors.AccentBlue,
                            contentColor = BlueGradientColors.BackgroundPrimary
                        )
                    ) {
                        Text(if (seat.studentName != null) "更新" else "分配")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "取消",
                        color = BlueGradientColors.SecondaryText
                    )
                }
            },
            containerColor = BlueGradientColors.BackgroundPrimary
        )
    }
}
