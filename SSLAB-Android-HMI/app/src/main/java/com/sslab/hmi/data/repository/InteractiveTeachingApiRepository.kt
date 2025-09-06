package com.sslab.hmi.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.sslab.hmi.data.network.InteractiveTeachingApiService
import com.sslab.hmi.data.network.InteractiveSession
import com.sslab.hmi.data.network.CreateSessionRequest
import com.sslab.hmi.data.network.PublishQuestionRequest
import com.sslab.hmi.data.network.SubmitAnswerRequest
import com.sslab.hmi.data.network.StudentInfo
import com.sslab.hmi.ui.screens.interactive.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 互动教学真实API数据仓库
 */
@Singleton
class InteractiveTeachingApiRepository @Inject constructor(
    private val apiService: InteractiveTeachingApiService
) {
    
    companion object {
        private const val TAG = "InteractiveTeachingApiRepo"
    }
    
    // 当前教学会话
    private val _currentSession = MutableStateFlow<InteractiveSession?>(null)
    val currentSession: StateFlow<InteractiveSession?> = _currentSession.asStateFlow()
    
    // 学生列表
    private val _students = MutableStateFlow<List<StudentSeat>>(emptyList())
    val students: StateFlow<List<StudentSeat>> = _students.asStateFlow()
    
    // 当前题目
    private val _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()
    
    // 题目激活状态
    private val _isQuestionActive = MutableStateFlow(false)
    val isQuestionActive: StateFlow<Boolean> = _isQuestionActive.asStateFlow()
    
    // 答题统计
    private val _statistics = MutableStateFlow<AnswerStatistics?>(null)
    val statistics: StateFlow<AnswerStatistics?> = _statistics.asStateFlow()
    
    // 学生答案记录
    private val _studentAnswers = MutableStateFlow<List<StudentAnswer>>(emptyList())
    val studentAnswers: StateFlow<List<StudentAnswer>> = _studentAnswers.asStateFlow()
    
    /**
     * 初始化教学会话
     */
    suspend fun initializeSession(): Result<InteractiveSession> {
        return try {
            // 先尝试获取当前会话
            val currentResponse = apiService.getCurrentSession()
            if (currentResponse.isSuccessful && currentResponse.body()?.success == true) {
                val session = currentResponse.body()?.data
                if (session != null) {
                    _currentSession.value = session
                    Log.d(TAG, "获取到现有会话: ${session.id}")
                    return Result.success(session)
                }
            }
            
            // 如果没有现有会话，创建新会话
            val createRequest = CreateSessionRequest(
                teacherName = "SSLAB老师",
                sessionName = "互动教学会话_${System.currentTimeMillis()}"
            )
            
            val response = apiService.createSession(createRequest)
            if (response.isSuccessful && response.body()?.success == true) {
                val newSession = response.body()?.data!!
                _currentSession.value = newSession
                
                // 启动会话
                val startResponse = apiService.startSession(newSession.id)
                if (startResponse.isSuccessful && startResponse.body()?.success == true) {
                    val startedSession = startResponse.body()?.data!!
                    _currentSession.value = startedSession
                    Log.d(TAG, "创建并启动新会话: ${startedSession.id}")
                    
                    // 初始化学生数据
                    loadStudents()
                    
                    return Result.success(startedSession)
                }
            }
            
            Result.failure(Exception("会话初始化失败"))
        } catch (e: Exception) {
            Log.e(TAG, "初始化会话异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 加载学生列表
     */
    suspend fun loadStudents() {
        try {
            val response = apiService.getStudents()
            if (response.isSuccessful && response.body()?.success == true) {
                val studentInfoList = response.body()?.data ?: emptyList()
                Log.d(TAG, "服务器返回学生数据: $studentInfoList")
                
                // 转换为StudentSeat格式
                val seatList = generateSeatsFromStudentInfo(studentInfoList)
                _students.value = seatList
                
                Log.d(TAG, "加载学生列表成功: ${studentInfoList.size}名学生，生成${seatList.size}个座位")
                Log.d(TAG, "座位详情: ${seatList.filter { it.studentName != null }}")
            } else {
                Log.w(TAG, "加载学生列表失败: ${response.body()?.message}")
                // 如果API失败，生成默认座位
                val defaultSeats = generateDefaultSeats()
                _students.value = defaultSeats
                Log.d(TAG, "使用默认座位: ${defaultSeats.filter { it.studentName != null }}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载学生列表异常", e)
            // 异常时生成默认座位
            val defaultSeats = generateDefaultSeats()
            _students.value = defaultSeats
            Log.d(TAG, "异常时使用默认座位: ${defaultSeats.filter { it.studentName != null }}")
        }
    }
    
    /**
     * 发布题目
     */
    suspend fun publishQuestion(question: Question): Result<Question> {
        return try {
            val request = PublishQuestionRequest(
                content = question.content,
                options = question.options,
                correctAnswer = question.correctAnswer,
                timeLimit = question.timeLimit,
                difficulty = "medium"
            )
            
            val response = apiService.publishQuestion(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val publishedQuestion = response.body()?.data!!
                _currentQuestion.value = publishedQuestion
                Log.d(TAG, "发布题目成功: ${publishedQuestion.id}")
                Result.success(publishedQuestion)
            } else {
                Result.failure(Exception("发布题目失败: ${response.body()?.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "发布题目异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 启动答题
     */
    suspend fun startQuestion(): Result<Unit> {
        return try {
            val currentQ = _currentQuestion.value
            if (currentQ == null) {
                return Result.failure(Exception("没有当前题目"))
            }
            
            val response = apiService.startQuestion(currentQ.id)
            if (response.isSuccessful && response.body()?.success == true) {
                _isQuestionActive.value = true
                Log.d(TAG, "启动答题成功: ${currentQ.id}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("启动答题失败: ${response.body()?.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "启动答题异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 停止答题
     */
    suspend fun stopQuestion(): Result<Unit> {
        return try {
            val currentQ = _currentQuestion.value
            if (currentQ == null) {
                return Result.failure(Exception("没有当前题目"))
            }
            
            val response = apiService.stopQuestion(currentQ.id)
            if (response.isSuccessful && response.body()?.success == true) {
                _isQuestionActive.value = false
                Log.d(TAG, "停止答题成功: ${currentQ.id}")
                
                // 刷新统计数据
                refreshStatistics()
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("停止答题失败: ${response.body()?.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "停止答题异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 清除答案
     */
    suspend fun clearAnswers(): Result<Unit> {
        return try {
            val currentQ = _currentQuestion.value
            if (currentQ == null) {
                return Result.failure(Exception("没有当前题目"))
            }
            
            val response = apiService.clearAnswers(currentQ.id)
            if (response.isSuccessful && response.body()?.success == true) {
                _studentAnswers.value = emptyList()
                _statistics.value = null
                
                // 重置座位状态
                resetSeatStatuses()
                
                Log.d(TAG, "清除答案成功: ${currentQ.id}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("清除答案失败: ${response.body()?.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "清除答案异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 刷新当前题目
     */
    suspend fun refreshCurrentQuestion() {
        try {
            Log.d(TAG, "开始刷新当前题目...")
            val response = apiService.getCurrentQuestion()
            Log.d(TAG, "API响应: 成功=${response.isSuccessful}, 代码=${response.code()}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val question = response.body()?.data
                _currentQuestion.value = question
                Log.d(TAG, "刷新当前题目成功: ${question?.id ?: "无"}, 内容: ${question?.content ?: "空"}")
            } else {
                Log.w(TAG, "刷新当前题目失败: ${response.body()?.message ?: "未知错误"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新当前题目异常", e)
        }
    }
    
    /**
     * 刷新答题统计
     */
    suspend fun refreshStatistics() {
        try {
            Log.d(TAG, "开始刷新统计数据...")
            // 使用综合统计API，不依赖特定题目ID
            val response = apiService.getInteractiveStatistics()
            if (response.isSuccessful && response.body()?.success == true) {
                val statsData = response.body()?.data
                if (statsData != null) {
                    // 提取当前题目的统计信息
                    val currentStats = statsData.currentQuestionStats
                    if (currentStats != null) {
                        val stats = AnswerStatistics(
                            totalAnswered = currentStats.totalAnswered ?: 0,
                            correctCount = currentStats.correctAnswers ?: 0,
                            correctAnswers = currentStats.correctAnswers ?: 0,
                            incorrectCount = currentStats.incorrectAnswers ?: 0,
                            timeoutCount = 0,
                            unanswered = currentStats.unanswered ?: 0,
                            fastestTime = currentStats.fastestTime
                        )
                        _statistics.value = stats
                        Log.d(TAG, "刷新统计数据成功: $stats")
                    } else {
                        Log.d(TAG, "当前无统计数据")
                        _statistics.value = null
                    }
                } else {
                    Log.w(TAG, "统计数据为空")
                }
            } else {
                Log.w(TAG, "刷新统计数据失败: ${response.body()?.message ?: "未知错误"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新统计数据异常", e)
        }
    }
    
    /**
     * 刷新学生答案
     */
    suspend fun refreshStudentAnswers() {
        try {
            val currentQ = _currentQuestion.value ?: return
            
            val response = apiService.getStudentAnswers(currentQ.id)
            if (response.isSuccessful && response.body()?.success == true) {
                val answers = response.body()?.data ?: emptyList()
                _studentAnswers.value = answers
                
                // 更新座位状态
                updateSeatsWithAnswers(answers)
                
                Log.d(TAG, "刷新学生答案: ${answers.size}个答案")
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新学生答案异常", e)
        }
    }
    
    /**
     * 分配学生到座位
     */
    suspend fun assignStudentToSeat(seatId: String, studentName: String): Result<Unit> {
        return try {
            // 暂时使用本地状态更新，等待API支持
            val updatedSeats = _students.value.map { seat ->
                if (seat.seatId == seatId) {
                    seat.copy(
                        studentName = studentName,
                        status = SeatStatus.WAITING
                    )
                } else {
                    seat
                }
            }
            _students.value = updatedSeats
            
            Log.d(TAG, "分配学生到座位: $studentName -> $seatId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "分配学生到座位异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 从座位移除学生
     */
    suspend fun removeStudentFromSeat(seatId: String): Result<Unit> {
        return try {
            val updatedSeats = _students.value.map { seat ->
                if (seat.seatId == seatId) {
                    seat.copy(
                        studentName = null,
                        status = SeatStatus.EMPTY,
                        lastAnswer = null,
                        responseTime = null,
                        isCorrect = null
                    )
                } else {
                    seat
                }
            }
            _students.value = updatedSeats
            
            Log.d(TAG, "移除座位学生: $seatId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "移除座位学生异常", e)
            Result.failure(e)
        }
    }
    
    // 私有辅助方法
    
    /**
     * 从StudentInfo生成StudentSeat
     */
    private fun generateSeatsFromStudentInfo(studentInfoList: List<StudentInfo>): List<StudentSeat> {
        // 首先生成所有48个空座位 (A1-H6)
        val seats = mutableListOf<StudentSeat>()
        val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        
        for (row in rows) {
            for (col in 1..6) {
                val seatId = "$row$col"
                seats.add(
                    StudentSeat(
                        seatId = seatId,
                        studentName = null,
                        status = SeatStatus.EMPTY,
                        lastAnswer = null,
                        responseTime = null,
                        isCorrect = null
                    )
                )
            }
        }
        
        // 将服务器返回的学生信息分配到相应座位
        studentInfoList.forEach { studentInfo ->
            Log.d(TAG, "处理学生信息: $studentInfo")
            val seatIndex = seats.indexOfFirst { it.seatId == studentInfo.seatId }
            if (seatIndex >= 0) {
                // 根据服务器状态映射到UI状态
                val seatStatus = when {
                    studentInfo.studentName.isNullOrEmpty() -> SeatStatus.EMPTY
                    studentInfo.status == "CORRECT" -> SeatStatus.CORRECT
                    studentInfo.status == "INCORRECT" -> SeatStatus.INCORRECT
                    studentInfo.status == "TIMEOUT" -> SeatStatus.TIMEOUT
                    studentInfo.status == "WAITING" && studentInfo.isOnline -> SeatStatus.WAITING
                    else -> SeatStatus.EMPTY
                }
                
                seats[seatIndex] = seats[seatIndex].copy(
                    studentName = studentInfo.studentName,
                    status = seatStatus,
                    lastAnswer = studentInfo.lastAnswer,
                    responseTime = studentInfo.responseTime?.toDouble(), // 转换Long到Double
                    isCorrect = studentInfo.isCorrect
                )
                Log.d(TAG, "分配学生到座位: ${studentInfo.seatId} -> ${studentInfo.studentName}, 状态: ${studentInfo.status} -> $seatStatus")
            }
        }
        
        return seats
    }
    
    /**
     * 生成默认座位 (A1-H6)
     */
    private fun generateDefaultSeats(): List<StudentSeat> {
        val seats = mutableListOf<StudentSeat>()
        val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        
        for (row in rows) {
            for (col in 1..6) {
                val seatId = "$row$col"
                seats.add(
                    StudentSeat(
                        seatId = seatId,
                        studentName = null,
                        status = SeatStatus.EMPTY,
                        lastAnswer = null,
                        responseTime = null,
                        isCorrect = null
                    )
                )
            }
        }
        
        // 预设一些学生用于演示
        val demoStudents = listOf(
            "A1" to "张小明", "A2" to "李小红", "A3" to "王小华", "A4" to "刘小军",
            "B2" to "陈小美", "B3" to "林小强", "C1" to "周小雨", "C4" to "黄小光"
        )
        
        demoStudents.forEach { (seatId, studentName) ->
            val index = seats.indexOfFirst { it.seatId == seatId }
            if (index >= 0) {
                seats[index] = seats[index].copy(
                    studentName = studentName,
                    status = SeatStatus.WAITING
                )
            }
        }
        
        return seats
    }
    
    /**
     * 根据答案更新座位状态
     */
    private fun updateSeatsWithAnswers(answers: List<StudentAnswer>) {
        val updatedSeats = _students.value.map { seat ->
            val answer = answers.find { it.seatId == seat.seatId }
            if (answer != null) {
                seat.copy(
                    status = if (answer.isCorrect) SeatStatus.CORRECT else SeatStatus.INCORRECT,
                    lastAnswer = answer.answer,
                    answerTime = answer.submittedAt,
                    responseTime = answer.responseTime,
                    isCorrect = answer.isCorrect
                )
            } else {
                seat
            }
        }
        _students.value = updatedSeats
    }
    
    /**
     * 重置座位状态
     */
    private fun resetSeatStatuses() {
        val updatedSeats = _students.value.map { seat ->
            if (seat.studentName != null) {
                seat.copy(
                    status = SeatStatus.WAITING,
                    lastAnswer = null,
                    responseTime = null,
                    isCorrect = null
                )
            } else {
                seat
            }
        }
        _students.value = updatedSeats
    }
    
    /**
     * 提交答案
     */
    suspend fun submitAnswer(seatId: String, answer: String): Result<Unit> {
        return try {
            val request = SubmitAnswerRequest(answer = answer)
            
            val response = apiService.submitAnswer(seatId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "座位 $seatId 答案提交成功: $answer")
                
                // 立即刷新答案和统计数据
                refreshStudentAnswers()
                refreshStatistics()
                
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.message ?: "提交失败"
                Log.w(TAG, "座位 $seatId 答案提交失败: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "提交答案异常", e)
            Result.failure(e)
        }
    }
}
