package com.sslab.hmi.data.network

import retrofit2.Response
import retrofit2.http.*
import com.sslab.hmi.ui.screens.interactive.*

/**
 * 互动教学API服务接口
 */
interface InteractiveTeachingApiService {
    
    // 获取当前教学会话
    @GET("/api/interactive/sessions/current")
    suspend fun getCurrentSession(): Response<ApiResponse<InteractiveSession?>>
    
    // 创建新的教学会话
    @POST("/api/interactive/sessions")
    suspend fun createSession(@Body request: CreateSessionRequest): Response<ApiResponse<InteractiveSession>>
    
    // 启动教学会话
    @POST("/api/interactive/sessions/{sessionId}/start")
    suspend fun startSession(@Path("sessionId") sessionId: String): Response<ApiResponse<InteractiveSession>>
    
    // 停止教学会话
    @POST("/api/interactive/sessions/{sessionId}/stop")
    suspend fun stopSession(@Path("sessionId") sessionId: String): Response<ApiResponse<InteractiveSession>>
    
    // 获取所有学生列表
    @GET("/api/interactive/students")
    suspend fun getStudents(): Response<ApiResponse<List<StudentInfo>>>
    
    // 分配学生到座位
    @POST("/api/interactive/students/{studentId}/seat/{seatId}")
    suspend fun assignStudentToSeat(
        @Path("studentId") studentId: String,
        @Path("seatId") seatId: String
    ): Response<ApiResponse<Unit>>
    
    // 获取当前题目
    @GET("/api/interactive/questions/current")
    suspend fun getCurrentQuestion(): Response<ApiResponse<Question?>>
    
    // 发布题目
    @POST("/api/interactive/questions")
    suspend fun publishQuestion(@Body question: PublishQuestionRequest): Response<ApiResponse<Question>>
    
    // 启动答题
    @POST("/api/interactive/questions/{questionId}/start")
    suspend fun startQuestion(@Path("questionId") questionId: String): Response<ApiResponse<Unit>>
    
    // 停止答题
    @POST("/api/interactive/questions/{questionId}/stop")
    suspend fun stopQuestion(@Path("questionId") questionId: String): Response<ApiResponse<Unit>>
    
    // 获取答题统计
    @GET("/api/interactive/questions/{questionId}/statistics")
    suspend fun getQuestionStatistics(@Path("questionId") questionId: String): Response<ApiResponse<AnswerStatistics>>
    
    // 获取综合统计信息
    @GET("/api/interactive/statistics")
    suspend fun getInteractiveStatistics(): Response<ApiResponse<InteractiveStatisticsData>>
    
    // 获取学生答案列表
    @GET("/api/interactive/questions/{questionId}/answers")
    suspend fun getStudentAnswers(@Path("questionId") questionId: String): Response<ApiResponse<List<StudentAnswer>>>
    
    // 清除当前答案
    @DELETE("/api/interactive/questions/{questionId}/answers")
    suspend fun clearAnswers(@Path("questionId") questionId: String): Response<ApiResponse<Unit>>
    
    // 学生提交答案
    @POST("/api/interactive/students/{seatId}/answer")
    suspend fun submitAnswer(@Path("seatId") seatId: String, @Body answer: SubmitAnswerRequest): Response<ApiResponse<Unit>>
}

// API请求和响应数据类
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)

data class CreateSessionRequest(
    val teacherName: String,
    val sessionName: String
)

data class PublishQuestionRequest(
    val content: String,
    val options: List<String>,
    val correctAnswer: String,
    val timeLimit: Int,
    val difficulty: String
)

data class SubmitAnswerRequest(
    val answer: String
)

data class InteractiveSession(
    val id: String,
    val teacherName: String,
    val sessionName: String,
    val status: String,
    val createdAt: String,
    val startedAt: String? = null,
    val stoppedAt: String? = null
)

data class StudentInfo(
    val seatId: String,
    val studentName: String? = null,
    val status: String? = null,
    val lastAnswer: String? = null,
    val responseTime: Long? = null,
    val isCorrect: Boolean? = null,
    val assignTime: Long? = null,
    val isOnline: Boolean = false
)

data class InteractiveStatisticsData(
    val currentQuestionId: String? = null,
    val currentQuestionStats: CurrentQuestionStats? = null,
    val totalStudents: Int = 0,
    val totalQuestions: Int = 0,
    val totalSessions: Int = 0,
    val activeSessions: Int = 0
)

data class CurrentQuestionStats(
    val totalAnswered: Int? = null,
    val correctAnswers: Int? = null,
    val incorrectAnswers: Int? = null,
    val unanswered: Int? = null,
    val averageResponseTime: Double? = null,
    val fastestTime: Double? = null,
    val slowestTime: Double? = null,
    val updateTime: Long? = null
)