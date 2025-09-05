package com.sslab.hmi.ui.screens.interactive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sslab.hmi.data.repository.TeachingPowerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * 学生答题数据
 */
data class StudentAnswer(
    val studentId: String,
    val seatPosition: String, // A1, A2, B1, B2, etc.
    val questionId: String,
    val answer: String,
    val timestamp: Long,
    val isCorrect: Boolean,
    val responseTime: Long // 答题用时（毫秒）
)

/**
 * 题目数据
 */
data class Question(
    val id: String,
    val content: String,
    val options: List<String>, // A, B, C, D选项
    val correctAnswer: String,
    val timeLimit: Int, // 答题时间限制（秒）
    val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM
)

/**
 * 题目难度等级
 */
enum class QuestionDifficulty(val label: String, val color: androidx.compose.ui.graphics.Color) {
    EASY("简单", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    MEDIUM("中等", androidx.compose.ui.graphics.Color(0xFF2196F3)),
    HARD("困难", androidx.compose.ui.graphics.Color(0xFFFF9800))
}

/**
 * 学生座位状态
 */
data class StudentSeat(
    val seatId: String, // A1, A2, B1, B2, etc.
    val studentName: String?,
    val status: SeatStatus,
    val lastAnswer: String?,
    val responseTime: Long?,
    val isCorrect: Boolean?
)

/**
 * 座位状态枚举
 */
enum class SeatStatus(val label: String, val color: androidx.compose.ui.graphics.Color) {
    EMPTY("空座", androidx.compose.ui.graphics.Color(0xFF9E9E9E)),
    WAITING("等待中", androidx.compose.ui.graphics.Color(0xFF607D8B)),
    ANSWERING("答题中", androidx.compose.ui.graphics.Color(0xFF2196F3)),
    ANSWERED_CORRECT("答对了", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    ANSWERED_WRONG("答错了", androidx.compose.ui.graphics.Color(0xFFFF5722)),
    TIMEOUT("超时", androidx.compose.ui.graphics.Color(0xFFFF9800))
}

/**
 * 答题统计数据
 */
data class AnswerStatistics(
    val totalStudents: Int,
    val answeredCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val timeoutCount: Int,
    val participationRate: Float, // 参与率
    val correctRate: Float, // 正确率
    val averageResponseTime: Long, // 平均答题时间
    val fastestStudent: String?, // 最快答题学生
    val fastestTime: Long?, // 最快答题时间
    val optionStatistics: Map<String, Int> // 各选项选择人数
)

/**
 * 互动教学ViewModel
 */
@HiltViewModel
class InteractiveTeachingViewModel @Inject constructor(
    private val repository: TeachingPowerRepository
) : ViewModel() {

    // 当前题目
    private val _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()

    // 学生座位状态 (A1-D4共16个座位)
    private val _studentSeats = MutableStateFlow(generateInitialSeats())
    val studentSeats: StateFlow<List<StudentSeat>> = _studentSeats.asStateFlow()

    // 答题记录
    private val _answers = MutableStateFlow<List<StudentAnswer>>(emptyList())
    val answers: StateFlow<List<StudentAnswer>> = _answers.asStateFlow()

    // 答题统计
    private val _statistics = MutableStateFlow<AnswerStatistics?>(null)
    val statistics: StateFlow<AnswerStatistics?> = _statistics.asStateFlow()

    // 答题倒计时
    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    // 是否正在答题
    private val _isQuestionActive = MutableStateFlow(false)
    val isQuestionActive: StateFlow<Boolean> = _isQuestionActive.asStateFlow()

    // 题目库
    private val questionBank = generateQuestionBank()

    init {
        // 初始化学生座位
        generateStudentNames()
    }

    /**
     * 发布新题目
     */
    fun publishQuestion(question: Question) {
        viewModelScope.launch {
            _currentQuestion.value = question
            _isQuestionActive.value = true
            _timeRemaining.value = question.timeLimit
            
            // 重置所有座位状态为等待中
            resetSeatStatuses()
            
            // 清空当前题目的答题记录
            clearCurrentQuestionAnswers()
            
            // 启动倒计时
            startCountdown(question.timeLimit)
        }
    }

    /**
     * 随机发布题目
     */
    fun publishRandomQuestion() {
        val randomQuestion = questionBank.random()
        publishQuestion(randomQuestion)
    }

    /**
     * 收集学生答案
     */
    fun submitAnswer(seatId: String, answer: String) {
        val currentQ = _currentQuestion.value ?: return
        if (!_isQuestionActive.value) return

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val responseTime = timestamp - (timestamp - (_timeRemaining.value * 1000))
            val isCorrect = answer == currentQ.correctAnswer
            
            val studentAnswer = StudentAnswer(
                studentId = seatId,
                seatPosition = seatId,
                questionId = currentQ.id,
                answer = answer,
                timestamp = timestamp,
                isCorrect = isCorrect,
                responseTime = responseTime
            )
            
            // 添加答题记录
            _answers.value = _answers.value + studentAnswer
            
            // 更新座位状态
            updateSeatStatus(seatId, answer, isCorrect, responseTime)
            
            // 更新统计数据
            updateStatistics()
        }
    }

    /**
     * 结束当前题目
     */
    fun endCurrentQuestion() {
        _isQuestionActive.value = false
        _timeRemaining.value = 0
        
        // 将未答题的座位设置为超时状态
        markUnansweredSeatsAsTimeout()
        
        // 最终统计更新
        updateStatistics()
    }

    /**
     * 获取随机题目（用于快速测试）
     */
    fun getRandomQuestion(): Question {
        return questionBank.random()
    }

    /**
     * 清空所有答题记录
     */
    fun clearAllAnswers() {
        _answers.value = emptyList()
        _statistics.value = null
        resetSeatStatuses()
    }

    // ==================== 私有方法 ====================

    /**
     * 生成初始座位布局 (A1-D4)
     */
    private fun generateInitialSeats(): List<StudentSeat> {
        val seats = mutableListOf<StudentSeat>()
        val rows = listOf("A", "B", "C", "D")
        
        for (row in rows) {
            for (col in 1..4) {
                val seatId = "$row$col"
                seats.add(
                    StudentSeat(
                        seatId = seatId,
                        studentName = null, // 稍后生成
                        status = SeatStatus.EMPTY,
                        lastAnswer = null,
                        responseTime = null,
                        isCorrect = null
                    )
                )
            }
        }
        return seats
    }

    /**
     * 生成学生姓名（模拟数据）
     */
    private fun generateStudentNames() {
        val names = listOf(
            "张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十",
            "郑一", "王二", "李三", "张四", "赵五", "钱六", "孙七", "周八"
        )
        
        val updatedSeats = _studentSeats.value.mapIndexed { index, seat ->
            seat.copy(
                studentName = names.getOrNull(index),
                status = if (names.getOrNull(index) != null) SeatStatus.WAITING else SeatStatus.EMPTY
            )
        }
        _studentSeats.value = updatedSeats
    }

    /**
     * 重置所有座位状态
     */
    private fun resetSeatStatuses() {
        val updatedSeats = _studentSeats.value.map { seat ->
            seat.copy(
                status = if (seat.studentName != null) SeatStatus.WAITING else SeatStatus.EMPTY,
                lastAnswer = null,
                responseTime = null,
                isCorrect = null
            )
        }
        _studentSeats.value = updatedSeats
    }

    /**
     * 更新特定座位状态
     */
    private fun updateSeatStatus(seatId: String, answer: String, isCorrect: Boolean, responseTime: Long) {
        val updatedSeats = _studentSeats.value.map { seat ->
            if (seat.seatId == seatId) {
                seat.copy(
                    status = if (isCorrect) SeatStatus.ANSWERED_CORRECT else SeatStatus.ANSWERED_WRONG,
                    lastAnswer = answer,
                    responseTime = responseTime,
                    isCorrect = isCorrect
                )
            } else {
                seat
            }
        }
        _studentSeats.value = updatedSeats
    }

    /**
     * 标记未答题座位为超时
     */
    private fun markUnansweredSeatsAsTimeout() {
        val updatedSeats = _studentSeats.value.map { seat ->
            if (seat.status == SeatStatus.WAITING || seat.status == SeatStatus.ANSWERING) {
                seat.copy(status = SeatStatus.TIMEOUT)
            } else {
                seat
            }
        }
        _studentSeats.value = updatedSeats
    }

    /**
     * 启动倒计时
     */
    private fun startCountdown(seconds: Int) {
        viewModelScope.launch {
            for (i in seconds downTo 0) {
                _timeRemaining.value = i
                delay(1000)
                if (!_isQuestionActive.value) break
            }
            if (_isQuestionActive.value) {
                endCurrentQuestion()
            }
        }
    }

    /**
     * 清空当前题目答题记录
     */
    private fun clearCurrentQuestionAnswers() {
        val currentQuestionId = _currentQuestion.value?.id
        if (currentQuestionId != null) {
            _answers.value = _answers.value.filter { it.questionId != currentQuestionId }
        }
    }

    /**
     * 更新答题统计
     */
    private fun updateStatistics() {
        val currentQuestionId = _currentQuestion.value?.id ?: return
        val currentAnswers = _answers.value.filter { it.questionId == currentQuestionId }
        val occupiedSeats = _studentSeats.value.filter { it.studentName != null }
        
        val totalStudents = occupiedSeats.size
        val answeredCount = currentAnswers.size
        val correctCount = currentAnswers.count { it.isCorrect }
        val wrongCount = answeredCount - correctCount
        val timeoutCount = _studentSeats.value.count { it.status == SeatStatus.TIMEOUT }
        
        val participationRate = if (totalStudents > 0) answeredCount.toFloat() / totalStudents else 0f
        val correctRate = if (answeredCount > 0) correctCount.toFloat() / answeredCount else 0f
        val averageResponseTime = if (currentAnswers.isNotEmpty()) {
            currentAnswers.map { it.responseTime }.average().toLong()
        } else 0L
        
        val fastestAnswer = currentAnswers.minByOrNull { it.responseTime }
        val fastestStudent = fastestAnswer?.seatPosition
        val fastestTime = fastestAnswer?.responseTime
        
        // 统计各选项选择人数
        val optionStats = currentAnswers.groupBy { it.answer }.mapValues { it.value.size }
        
        _statistics.value = AnswerStatistics(
            totalStudents = totalStudents,
            answeredCount = answeredCount,
            correctCount = correctCount,
            wrongCount = wrongCount,
            timeoutCount = timeoutCount,
            participationRate = participationRate,
            correctRate = correctRate,
            averageResponseTime = averageResponseTime,
            fastestStudent = fastestStudent,
            fastestTime = fastestTime,
            optionStatistics = optionStats
        )
    }

    /**
     * 生成题目库（示例题目）
     */
    private fun generateQuestionBank(): List<Question> {
        return listOf(
            Question(
                id = "Q001",
                content = "在串联电路中，电流的特点是？",
                options = listOf("A. 各处电流相等", "B. 各处电流不等", "C. 电流逐渐减小", "D. 电流逐渐增大"),
                correctAnswer = "A",
                timeLimit = 30,
                difficulty = QuestionDifficulty.EASY
            ),
            Question(
                id = "Q002", 
                content = "欧姆定律的数学表达式是？",
                options = listOf("A. U=I×R", "B. I=U×R", "C. R=U×I", "D. P=U×I"),
                correctAnswer = "A",
                timeLimit = 25,
                difficulty = QuestionDifficulty.EASY
            ),
            Question(
                id = "Q003",
                content = "在并联电路中，电压的特点是？",
                options = listOf("A. 各支路电压不等", "B. 各支路电压相等", "C. 电压逐渐减小", "D. 电压逐渐增大"),
                correctAnswer = "B",
                timeLimit = 30,
                difficulty = QuestionDifficulty.MEDIUM
            ),
            Question(
                id = "Q004",
                content = "电阻R1=10Ω，R2=20Ω串联，总电阻是？",
                options = listOf("A. 10Ω", "B. 20Ω", "C. 30Ω", "D. 15Ω"),
                correctAnswer = "C",
                timeLimit = 45,
                difficulty = QuestionDifficulty.MEDIUM
            ),
            Question(
                id = "Q005",
                content = "功率公式P=U²/R中，当U不变时，R越大，P如何变化？",
                options = listOf("A. P越大", "B. P越小", "C. P不变", "D. 无法确定"),
                correctAnswer = "B",
                timeLimit = 40,
                difficulty = QuestionDifficulty.HARD
            )
        )
    }
}
