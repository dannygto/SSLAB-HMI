package com.sslab.hmi.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import com.sslab.hmi.ui.screens.interactive.Question
import com.sslab.hmi.ui.screens.interactive.QuestionDifficulty
import com.sslab.hmi.ui.screens.interactive.StudentAnswer
import com.sslab.hmi.ui.screens.interactive.StudentSeat
import com.sslab.hmi.ui.screens.interactive.SeatStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 互动教学数据仓库
 * 管理学生答题数据、题目数据和统计信息
 */
@Singleton
class InteractiveTeachingRepository @Inject constructor() {
    
    // 学生座位数据
    private val _students = MutableStateFlow(generateInitialSeats())
    val students: StateFlow<List<StudentSeat>> = _students.asStateFlow()
    
    // 当前题目
    private val _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()
    
    // 题目激活状态
    private val _isQuestionActive = MutableStateFlow(false)
    val isQuestionActive: StateFlow<Boolean> = _isQuestionActive.asStateFlow()
    
    // 学生答案记录
    private val _studentAnswers = MutableStateFlow<List<StudentAnswer>>(emptyList())
    val studentAnswers: StateFlow<List<StudentAnswer>> = _studentAnswers.asStateFlow()
    
    /**
     * 生成初始的16个座位 (A1-D4)
     */
    private fun generateInitialSeats(): List<StudentSeat> {
        val seats = mutableListOf<StudentSeat>()
        val rows = listOf("A", "B", "C", "D")
        val cols = listOf(1, 2, 3, 4)
        
        // 预设一些学生名单作为演示
        val demoStudents = mapOf(
            "A1" to "张小明",
            "A2" to "李小红", 
            "A3" to "王小华",
            "A4" to "刘小军",
            "B1" to "陈小美",
            "B2" to "林小强",
            "B3" to "吴小梅", 
            "B4" to "郑小伟",
            "C1" to "黄小花",
            "C2" to "周小杰",
            "C3" to "何小敏",
            "C4" to "胡小波",
            "D1" to "谢小丽",
            "D2" to "潘小龙",
            "D3" to "曾小燕",
            "D4" to "罗小刚"
        )
        
        for (row in rows) {
            for (col in cols) {
                val seatId = "$row$col"
                val studentName = demoStudents[seatId]
                seats.add(
                    StudentSeat(
                        seatId = seatId,
                        studentName = studentName,
                        status = if (studentName != null) SeatStatus.OCCUPIED else SeatStatus.EMPTY,
                        lastAnswer = null,
                        lastAnswerTime = null
                    )
                )
            }
        }
        
        return seats
    }
    
    /**
     * 发布新题目
     */
    suspend fun publishQuestion(question: Question) {
        _currentQuestion.value = question
        _isQuestionActive.value = false
        // 清除之前的答案
        clearAnswers()
    }
    
    /**
     * 开始答题
     */
    suspend fun startQuestion() {
        if (_currentQuestion.value != null) {
            _isQuestionActive.value = true
        }
    }
    
    /**
     * 停止答题
     */
    suspend fun stopQuestion() {
        _isQuestionActive.value = false
    }
    
    /**
     * 学生提交答案
     */
    suspend fun submitAnswer(seatId: String, answer: String) {
        val currentQuestion = _currentQuestion.value ?: return
        if (!_isQuestionActive.value) return
        
        val currentTime = System.currentTimeMillis()
        val seat = _students.value.find { it.seatId == seatId } ?: return
        
        // 记录学生答案
        val studentAnswer = StudentAnswer(
            questionId = currentQuestion.id,
            seatId = seatId,
            studentName = seat.studentName ?: "未知学生",
            answer = answer,
            isCorrect = answer == currentQuestion.correctAnswer,
            timestamp = currentTime,
            answerTime = currentTime // 简化处理，实际应该记录开始答题时间
        )
        
        // 更新答案列表
        val updatedAnswers = _studentAnswers.value.toMutableList()
        // 移除该学生的旧答案
        updatedAnswers.removeAll { it.seatId == seatId && it.questionId == currentQuestion.id }
        // 添加新答案
        updatedAnswers.add(studentAnswer)
        _studentAnswers.value = updatedAnswers
        
        // 更新座位状态
        updateSeatStatus(seatId, answer, currentTime, answer == currentQuestion.correctAnswer)
    }
    
    /**
     * 更新座位状态
     */
    private suspend fun updateSeatStatus(seatId: String, answer: String, answerTime: Long, isCorrect: Boolean) {
        val updatedSeats = _students.value.map { seat ->
            if (seat.seatId == seatId && seat.studentName != null) {
                seat.copy(
                    status = if (isCorrect) SeatStatus.CORRECT else SeatStatus.INCORRECT,
                    lastAnswer = answer,
                    lastAnswerTime = answerTime
                )
            } else {
                seat
            }
        }
        _students.value = updatedSeats
    }
    
    /**
     * 清除答案
     */
    suspend fun clearAnswers() {
        _studentAnswers.value = emptyList()
        
        // 重置所有座位状态
        val resetSeats = _students.value.map { seat ->
            seat.copy(
                status = if (seat.studentName != null) SeatStatus.OCCUPIED else SeatStatus.EMPTY,
                lastAnswer = null,
                lastAnswerTime = null
            )
        }
        _students.value = resetSeats
    }
    
    /**
     * 分配学生到座位
     */
    suspend fun assignStudentToSeat(seatId: String, studentName: String) {
        val updatedSeats = _students.value.map { seat ->
            if (seat.seatId == seatId) {
                seat.copy(
                    studentName = studentName,
                    status = SeatStatus.OCCUPIED,
                    lastAnswer = null,
                    lastAnswerTime = null
                )
            } else {
                seat
            }
        }
        _students.value = updatedSeats
    }
    
    /**
     * 从座位移除学生
     */
    suspend fun removeStudentFromSeat(seatId: String) {
        val updatedSeats = _students.value.map { seat ->
            if (seat.seatId == seatId) {
                seat.copy(
                    studentName = null,
                    status = SeatStatus.EMPTY,
                    lastAnswer = null,
                    lastAnswerTime = null
                )
            } else {
                seat
            }
        }
        _students.value = updatedSeats
    }
    
    /**
     * 模拟学生随机答题 (演示用)
     */
    suspend fun simulateRandomAnswers() {
        val currentQuestion = _currentQuestion.value ?: return
        if (!_isQuestionActive.value) return
        
        val occupiedSeats = _students.value.filter { it.studentName != null }
        val options = listOf("A", "B", "C", "D")
        
        // 随机让一些学生答题
        val answeringStudents = occupiedSeats.shuffled().take((2..8).random())
        
        for (seat in answeringStudents) {
            delay((500..2000).random().toLong()) // 模拟思考时间
            val randomAnswer = if (Math.random() < 0.7) { // 70%概率答对
                currentQuestion.correctAnswer
            } else {
                options.random()
            }
            submitAnswer(seat.seatId, randomAnswer)
        }
    }
    
    /**
     * 刷新数据 (模拟网络刷新)
     */
    suspend fun refreshData() {
        delay(500) // 模拟网络延迟
        // 在实际应用中，这里会从服务器重新获取数据
    }
    
    /**
     * 获取当前题目的学生答案
     */
    fun getCurrentQuestionAnswers(): Flow<List<StudentAnswer>> {
        return MutableStateFlow(
            _studentAnswers.value.filter { 
                it.questionId == _currentQuestion.value?.id 
            }
        ).asStateFlow()
    }
}

/**
 * 演示题目数据
 */
object DemoQuestions {
    val sampleQuestions = listOf(
        Question(
            id = "1",
            content = "在Java中，下列哪个关键字用于定义常量？",
            options = listOf("const", "final", "static", "immutable"),
            correctAnswer = "B",
            difficulty = QuestionDifficulty.EASY,
            timeLimit = 30
        ),
        Question(
            id = "2", 
            content = "哪种排序算法的平均时间复杂度是O(n log n)？",
            options = listOf("冒泡排序", "插入排序", "快速排序", "选择排序"),
            correctAnswer = "C",
            difficulty = QuestionDifficulty.MEDIUM,
            timeLimit = 45
        ),
        Question(
            id = "3",
            content = "在数据库中，ACID特性不包括以下哪一项？",
            options = listOf("原子性(Atomicity)", "一致性(Consistency)", "隔离性(Isolation)", "可重复性(Repeatability)"),
            correctAnswer = "D", 
            difficulty = QuestionDifficulty.HARD,
            timeLimit = 60
        )
    )
}
