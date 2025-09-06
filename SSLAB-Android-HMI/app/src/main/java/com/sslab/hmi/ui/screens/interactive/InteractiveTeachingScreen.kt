package com.sslab.hmi.ui.screens.interactive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cn.sslab.hmi.ui.theme.BlueGradientColors

/**
 * 互动教学主界面 - 优化为一屏显示
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
    
    // 初始化时自动连接API
    LaunchedEffect(Unit) {
        viewModel.initializeConnection()
    }
    
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
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = BlueGradientColors.PrimaryText
                        )
                    }
                    
                    // 发布题目按钮
                    IconButton(onClick = { showQuestionDialog = true }) {
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
        }
    ) { paddingValues ->
        // 使用Row布局实现左右分布，避免滚动
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 左侧：题目和控制面板 (40%宽度)
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 当前题目卡片 - 紧凑版
                CompactQuestionCard(
                    question = uiState.currentQuestion,
                    timeRemaining = timeRemaining,
                    isActive = uiState.isQuestionActive,
                    modifier = Modifier.weight(0.6f)
                )
                
                // 控制按钮 - 紧凑版
                CompactControlButtons(
                    currentQuestion = uiState.currentQuestion,
                    isQuestionActive = uiState.isQuestionActive,
                    onStartQuestion = { viewModel.startQuestion() },
                    onStopQuestion = { viewModel.stopQuestion() },
                    onClearAnswers = { viewModel.clearAnswers() },
                    onPublishQuestion = { showQuestionDialog = true },
                    onCreateTestQuestion = { viewModel.createTestQuestion() },
                    modifier = Modifier.weight(0.4f)
                )
            }
            
            // 右侧：学生座位网格和统计 (60%宽度)
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 答题统计 - 紧凑版
                CompactAnswerStatistics(
                    statistics = statistics,
                    modifier = Modifier.height(80.dp)
                )
                
                // 学生座位网格 - 主要显示区域
                CompactStudentSeatsGrid(
                    seats = uiState.students,
                    onSeatClick = { seatId -> showSeatInfoDialog = seatId },
                    modifier = Modifier.weight(1f)
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
 * 紧凑版题目卡片
 */
@Composable
fun CompactQuestionCard(
    question: Question?,
    timeRemaining: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
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
            // 题目标题和计时器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (question != null) "当前题目" else "暂无题目",
                    style = MaterialTheme.typography.titleSmall,
                    color = BlueGradientColors.PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                
                if (question != null && isActive) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (timeRemaining <= 10) 
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f) 
                            else BlueGradientColors.AccentBlue.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "倒计时",
                                tint = if (timeRemaining <= 10) 
                                    MaterialTheme.colorScheme.error 
                                else BlueGradientColors.AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${timeRemaining}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (timeRemaining <= 10) 
                                    MaterialTheme.colorScheme.error 
                                else BlueGradientColors.AccentBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (question != null) {
                // 题目内容 - 限制高度
                Text(
                    text = question.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlueGradientColors.PrimaryText,
                    maxLines = 3,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 选项 - 紧凑显示
                question.options.forEachIndexed { index, option ->
                    val optionLabel = ('A' + index).toString()
                    Text(
                        text = "$optionLabel. $option",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (optionLabel == question.correctAnswer) 
                            BlueGradientColors.AccentGreen 
                        else BlueGradientColors.SecondaryText,
                        maxLines = 1
                    )
                }
            } else {
                // 无题目状态
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
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
 * 紧凑版控制按钮
 */
@Composable
fun CompactControlButtons(
    currentQuestion: Question?,
    isQuestionActive: Boolean,
    onStartQuestion: () -> Unit,
    onStopQuestion: () -> Unit,
    onClearAnswers: () -> Unit,
    onPublishQuestion: () -> Unit,
    onCreateTestQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "控制面板",
                style = MaterialTheme.typography.titleSmall,
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Bold
            )
            
            // 第一行按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 发布题目
                Button(
                    onClick = onPublishQuestion,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGradientColors.AccentBlue
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "发布",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                // 开始/停止答题
                if (currentQuestion != null) {
                    if (isQuestionActive) {
                        Button(
                            onClick = onStopQuestion,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "停止",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    } else {
                        Button(
                            onClick = onStartQuestion,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlueGradientColors.AccentGreen
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "开始",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
            
            // 测试按钮行
            Button(
                onClick = onCreateTestQuestion,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "创建测试题目",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            // 第二行按钮
            if (currentQuestion != null) {
                Button(
                    onClick = onClearAnswers,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGradientColors.SecondaryText.copy(alpha = 0.1f),
                        contentColor = BlueGradientColors.SecondaryText
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "清除答案",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            // 状态指示器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusIndicator(
                    icon = if (currentQuestion != null) Icons.Default.Check else Icons.Default.Close,
                    text = if (currentQuestion != null) "有题目" else "无题目",
                    color = if (currentQuestion != null) BlueGradientColors.AccentGreen else BlueGradientColors.SecondaryText,
                    modifier = Modifier.weight(1f)
                )
                
                StatusIndicator(
                    icon = if (isQuestionActive) Icons.Default.PlayArrow else Icons.Default.Pause,
                    text = if (isQuestionActive) "答题中" else "已停止",
                    color = if (isQuestionActive) BlueGradientColors.AccentBlue else BlueGradientColors.SecondaryText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 状态指示器
 */
@Composable
fun StatusIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 紧凑版答题统计
 */
@Composable
fun CompactAnswerStatistics(
    statistics: AnswerStatistics?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BlueGradientColors.BackgroundPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "答题统计",
                style = MaterialTheme.typography.titleSmall,
                color = BlueGradientColors.PrimaryText,
                fontWeight = FontWeight.Bold
            )
            
            if (statistics != null) {
                StatItem("总计", statistics.totalAnswered.toString(), BlueGradientColors.PrimaryText)
                StatItem("正确", statistics.correctCount.toString(), BlueGradientColors.AccentGreen)
                StatItem("错误", statistics.incorrectCount.toString(), MaterialTheme.colorScheme.error)
                StatItem("超时", statistics.timeoutCount.toString(), BlueGradientColors.AccentOrange)
            } else {
                Text(
                    text = "暂无答题数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BlueGradientColors.SecondaryText
                )
            }
        }
    }
}

/**
 * 统计项目
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = BlueGradientColors.SecondaryText
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
                            containerColor = when (seat.status) {
                                SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.1f)
                                SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue.copy(alpha = 0.1f)
                                SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green.copy(alpha = 0.1f)
                                SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.1f)
                                SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow.copy(alpha = 0.1f)
                            }
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
                                tint = when (seat.status) {
                                    SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray
                                    SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue
                                    SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green
                                    SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red
                                    SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "状态: ${when (seat.status) {
                                    SeatStatus.EMPTY -> "空座"
                                    SeatStatus.WAITING -> "等待"
                                    SeatStatus.CORRECT -> "正确"
                                    SeatStatus.INCORRECT -> "错误"
                                    SeatStatus.TIMEOUT -> "超时"
                                }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when (seat.status) {
                                    SeatStatus.EMPTY -> androidx.compose.ui.graphics.Color.Gray
                                    SeatStatus.WAITING -> androidx.compose.ui.graphics.Color.Blue
                                    SeatStatus.CORRECT -> androidx.compose.ui.graphics.Color.Green
                                    SeatStatus.INCORRECT -> androidx.compose.ui.graphics.Color.Red
                                    SeatStatus.TIMEOUT -> androidx.compose.ui.graphics.Color.Yellow
                                },
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
                    if (seat.lastAnswer != null && seat.responseTime != null) {
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
                                    text = "答题时间: ${seat.responseTime / 1000.0}秒",
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
