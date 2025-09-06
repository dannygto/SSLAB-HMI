package com.sslab.hmi.data.repository

impo    /**
     * 生成初始的48个座位 (A1-H6)
     */
    private fun generateInitialSeats(): List<StudentSeat> {
        val seats = mutableListOf<StudentSeat>()
        val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        val cols = listOf(1, 2, 3, 4, 5, 6)inx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import com.sslab.hmi.ui.screens.interactive.*
import com.sslab.hmi.data.network.InteractiveTeachingApiService
import com.sslab.hmi.data.network.CreateSessionRequest
import com.sslab.hmi.data.network.PublishQuestionRequest
import com.sslab.hmi.data.network.SubmitAnswerRequest
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 简化的互动教学数据仓库 - 使用真实API
 */
@Singleton
class InteractiveTeachingRepositorySimple @Inject constructor(
    private val apiService: InteractiveTeachingApiService
) {
    
    companion object {
        private const val TAG = "InteractiveTeachingRepo"
    }
    
    // 学生座位数据
    private val _students = MutableStateFlow(generateInitialSeats())
    val students: StateFlow<List<StudentSeat>> = _students.asStateFlow()
    
    // 当前题目
    private val _currentQuestion = MutableStateFlow<Question?>(null)
    val currentQuestion: StateFlow<Question?> = _currentQuestion.asStateFlow()
    
    // 题目激活状态
    private val _isQuestionActive = MutableStateFlow(false)
    val isQuestionActive: StateFlow<Boolean> = _isQuestionActive.asStateFlow()
    
    // 当前会话
    private val _currentSession = MutableStateFlow<com.sslab.hmi.data.network.InteractiveSession?>(null)
    val currentSession: StateFlow<com.sslab.hmi.data.network.InteractiveSession?> = _currentSession.asStateFlow()
    
    /**
     * 生成初始的48个座位 (A1-H6)
     */
    private fun generateInitialSeats(): List<StudentSeat> {
        val seats = mutableListOf<StudentSeat>()
        val rows = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        val cols = listOf(1, 2, 3, 4, 5, 6)
        
        rows.forEach { row ->
            cols.forEach { col ->
                seats.add(
                    StudentSeat(
                        seatId = "$row$col",
                        studentName = null,
                        status = SeatStatus.EMPTY,
                        lastAnswer = null,
                        answerTime = null
                    )
                )
            }
        }
        
        return seats
    }
    
    /**
     * 初始化或获取当前会话
     */
    suspend fun initializeSession(): Result<com.sslab.hmi.data.network.InteractiveSession> {
        return try {
            // 首先尝试获取当前会话
            val currentResponse = apiService.getCurrentSession()
            if (currentResponse.isSuccessful && currentResponse.body()?.success == true) {
                val session = currentResponse.body()?.data
                if (session != null) {
                    _currentSession.value = session
                    Log.d(TAG, "已获取当前会话: ${session.id}")
                    return Result.success(session)
                }
            }
            
            // 如果没有当前会话，创建新会话
            val createRequest = CreateSessionRequest(
                teacherName = "教师",
                sessionName = "互动教学会话_${System.currentTimeMillis()}"
            )
            
            val createResponse = apiService.createSession(createRequest)
            if (createResponse.isSuccessful && createResponse.body()?.success == true) {
                val newSession = createResponse.body()?.data!!
                _currentSession.value = newSession
                Log.d(TAG, "已创建新会话: ${newSession.id}")
                
                // 启动会话
                val startResponse = apiService.startSession(newSession.id)
                if (startResponse.isSuccessful) {
                    Log.d(TAG, "已启动会话: ${newSession.id}")
                }
                
                Result.success(newSession)
            } else {
                val error = "创建会话失败: ${createResponse.body()?.message}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "初始化会话失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 刷新当前题目
     */
    suspend fun refreshCurrentQuestion(): Result<Question?> {
        return try {
            val response = apiService.getCurrentQuestion()
            if (response.isSuccessful && response.body()?.success == true) {
                val question = response.body()?.data
                _currentQuestion.value = question
                Log.d(TAG, "已刷新当前题目: ${question?.id}")
                Result.success(question)
            } else {
                val error = "获取当前题目失败: ${response.body()?.message}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新题目失败", e)
            Result.failure(e)
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
                difficulty = question.difficulty.name
            )
            
            val response = apiService.publishQuestion(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val publishedQuestion = response.body()?.data!!
                _currentQuestion.value = publishedQuestion
                Log.d(TAG, "已发布题目: ${publishedQuestion.id}")
                Result.success(publishedQuestion)
            } else {
                val error = "发布题目失败: ${response.body()?.message}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "发布题目失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 启动答题
     */
    suspend fun startQuestion(): Result<Unit> {
        val question = _currentQuestion.value ?: return Result.failure(Exception("没有当前题目"))
        
        return try {
            val response = apiService.startQuestion(question.id)
            if (response.isSuccessful && response.body()?.success == true) {
                _isQuestionActive.value = true
                Log.d(TAG, "已启动答题: ${question.id}")
                Result.success(Unit)
            } else {
                val error = "启动答题失败: ${response.body()?.message}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "启动答题失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 停止答题
     */
    suspend fun stopQuestion(): Result<Unit> {
        val question = _currentQuestion.value ?: return Result.failure(Exception("没有当前题目"))
        
        return try {
            val response = apiService.stopQuestion(question.id)
            if (response.isSuccessful && response.body()?.success == true) {
                _isQuestionActive.value = false
                Log.d(TAG, "已停止答题: ${question.id}")
                Result.success(Unit)
            } else {
                val error = "停止答题失败: ${response.body()?.message}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "停止答题失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取答题统计
     */
    suspend fun getAnswerStatistics(): Result<AnswerStatistics?> {
        val question = _currentQuestion.value ?: return Result.success(null)
        
        return try {
            val response = apiService.getQuestionStatistics(question.id)
            if (response.isSuccessful && response.body()?.success == true) {
                val statistics = response.body()?.data
                Log.d(TAG, "已获取答题统计: $statistics")
                Result.success(statistics)
            } else {
                val error = "获取答题统计失败: ${response.body()?.message}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取答题统计失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取学生答案并更新座位状态
     */
    suspend fun refreshStudentAnswers(): Result<List<StudentAnswer>> {
        val question = _currentQuestion.value ?: return Result.success(emptyList())
        
        return try {
            val response = apiService.getStudentAnswers(question.id)
            if (response.isSuccessful && response.body()?.success == true) {
                val answers = response.body()?.data ?: emptyList()
                
                // 更新座位状态
                updateSeatsWithAnswers(answers, question.correctAnswer)
                
                Log.d(TAG, "已刷新学生答案: ${answers.size}个")
                Result.success(answers)
            } else {
                val error = "获取学生答案失败: ${response.body()?.message}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "刷新学生答案失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 清除答案
     */
    suspend fun clearAnswers(): Result<Unit> {
        val question = _currentQuestion.value ?: return Result.failure(Exception("没有当前题目"))
        
        return try {
            val response = apiService.clearAnswers(question.id)
            if (response.isSuccessful && response.body()?.success == true) {
                // 重置所有座位状态
                val updatedSeats = _students.value.map { seat ->
                    if (seat.studentName != null) {
                        seat.copy(status = SeatStatus.WAITING, lastAnswer = null, answerTime = null)
                    } else {
                        seat.copy(status = SeatStatus.EMPTY, lastAnswer = null, answerTime = null)
                    }
                }
                _students.value = updatedSeats
                
                Log.d(TAG, "已清除答案: ${question.id}")
                Result.success(Unit)
            } else {
                val error = "清除答案失败: ${response.body()?.message}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "清除答案失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 更新座位状态
     */
    private fun updateSeatsWithAnswers(answers: List<StudentAnswer>, correctAnswer: String) {
        val updatedSeats = _students.value.map { seat ->
            val studentAnswer = answers.find { it.seatId == seat.seatId }
            if (studentAnswer != null) {
                val status = when {
                    studentAnswer.answer == correctAnswer -> SeatStatus.CORRECT
                    studentAnswer.isTimeout -> SeatStatus.TIMEOUT
                    else -> SeatStatus.INCORRECT
                }
                seat.copy(
                    status = status,
                    lastAnswer = studentAnswer.answer,
                    answerTime = studentAnswer.submittedAt
                )
            } else if (seat.studentName != null) {
                // 有学生但没有答案，显示等待状态
                seat.copy(status = SeatStatus.WAITING, lastAnswer = null, answerTime = null)
            } else {
                seat
            }
        }
        _students.value = updatedSeats
    }
    
    /**
     * 分配学生到座位（模拟数据）
     */
    fun assignStudentToSeat(seatId: String, studentName: String) {
        val updatedSeats = _students.value.map { seat ->
            if (seat.seatId == seatId) {
                seat.copy(
                    studentName = studentName,
                    status = if (_currentQuestion.value != null) SeatStatus.WAITING else SeatStatus.EMPTY
                )
            } else {
                seat
            }
        }
        _students.value = updatedSeats
        Log.d(TAG, "已分配学生 $studentName 到座位 $seatId")
    }
    
    /**
     * 从座位移除学生
     */
    fun removeStudentFromSeat(seatId: String) {
        val updatedSeats = _students.value.map { seat ->
            if (seat.seatId == seatId) {
                seat.copy(
                    studentName = null,
                    status = SeatStatus.EMPTY,
                    lastAnswer = null,
                    answerTime = null
                )
            } else {
                seat
            }
        }
        _students.value = updatedSeats
        Log.d(TAG, "已从座位 $seatId 移除学生")
    }
}