package com.sslab.hmi.ui.screens.environment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sslab.hmi.data.repository.TeachingPowerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import javax.inject.Inject

/**
 * 环境数据
 */
data class EnvironmentData(
    val temperature: Float = 23.5f,
    val humidity: Float = 45.2f,
    val co2: Float = 450f,
    val pm25: Float = 12f,
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
) {
    fun getTemperatureLevel(): AirQualityLevel {
        return when {
            temperature < 16 || temperature > 30 -> AirQualityLevel.POOR
            temperature < 18 || temperature > 28 -> AirQualityLevel.MODERATE
            temperature < 20 || temperature > 26 -> AirQualityLevel.GOOD
            else -> AirQualityLevel.EXCELLENT
        }
    }
    
    fun getHumidityLevel(): AirQualityLevel {
        return when {
            humidity < 30 || humidity > 80 -> AirQualityLevel.POOR
            humidity < 40 || humidity > 70 -> AirQualityLevel.MODERATE
            humidity < 45 || humidity > 65 -> AirQualityLevel.GOOD
            else -> AirQualityLevel.EXCELLENT
        }
    }
    
    fun getCO2Level(): AirQualityLevel {
        return when {
            co2 > 1000 -> AirQualityLevel.POOR
            co2 > 800 -> AirQualityLevel.MODERATE
            co2 > 600 -> AirQualityLevel.GOOD
            else -> AirQualityLevel.EXCELLENT
        }
    }
    
    fun getPM25Level(): AirQualityLevel {
        return when {
            pm25 > 75 -> AirQualityLevel.VERY_POOR
            pm25 > 50 -> AirQualityLevel.POOR
            pm25 > 35 -> AirQualityLevel.MODERATE
            pm25 > 15 -> AirQualityLevel.GOOD
            else -> AirQualityLevel.EXCELLENT
        }
    }
}

/**
 * 时间范围枚举
 */
enum class TimeRange(val label: String, val hours: Int) {
    HOUR_1("1小时", 1),
    HOUR_6("6小时", 6),
    HOUR_12("12小时", 12),
    DAY_1("1天", 24),
    DAY_7("7天", 168)
}

/**
 * 环境监测ViewModel
 */
@HiltViewModel
class EnvironmentMonitoringViewModel @Inject constructor(
    private val repository: TeachingPowerRepository
) : ViewModel() {

    private val _currentEnvironmentData = MutableStateFlow(generateMockEnvironmentData())
    val currentEnvironmentData: StateFlow<EnvironmentData> = _currentEnvironmentData.asStateFlow()

    private val _historyData = MutableStateFlow<List<EnvironmentData>>(emptyList())
    val historyData: StateFlow<List<EnvironmentData>> = _historyData.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.HOUR_1)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // 启动自动刷新
        startAutoRefresh()
        // 生成初始历史数据
        generateHistoryData()
    }

    /**
     * 启动自动刷新
     */
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5000) // 每5秒刷新一次
                _currentEnvironmentData.value = generateMockEnvironmentData()
                updateHistoryData()
            }
        }
    }

    /**
     * 手动刷新数据
     */
    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1000) // 模拟网络请求
            _currentEnvironmentData.value = generateMockEnvironmentData()
            updateHistoryData()
            _isRefreshing.value = false
        }
    }

    /**
     * 设置时间范围
     */
    fun setTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
        generateHistoryData()
    }

    /**
     * 生成历史数据
     */
    private fun generateHistoryData() {
        val selectedRange = _selectedTimeRange.value
        val dataPoints = when (selectedRange) {
            TimeRange.HOUR_1 -> 12  // 5分钟间隔
            TimeRange.HOUR_6 -> 36  // 10分钟间隔
            TimeRange.HOUR_12 -> 48 // 15分钟间隔
            TimeRange.DAY_1 -> 48   // 30分钟间隔
            TimeRange.DAY_7 -> 168  // 1小时间隔
        }
        
        _historyData.value = generateMockHistoryData(dataPoints)
    }

    /**
     * 更新历史数据
     */
    private fun updateHistoryData() {
        val currentHistory = _historyData.value.toMutableList()
        currentHistory.add(_currentEnvironmentData.value)
        
        // 根据时间范围限制数据点数量
        val maxDataPoints = when (_selectedTimeRange.value) {
            TimeRange.HOUR_1 -> 12
            TimeRange.HOUR_6 -> 36
            TimeRange.HOUR_12 -> 48
            TimeRange.DAY_1 -> 48
            TimeRange.DAY_7 -> 168
        }
        
        if (currentHistory.size > maxDataPoints) {
            currentHistory.removeAt(0)
        }
        
        _historyData.value = currentHistory
    }

    /**
     * 生成模拟环境数据
     */
    private fun generateMockEnvironmentData(): EnvironmentData {
        return EnvironmentData(
            temperature = 20f + Random.nextFloat() * 8f, // 20-28°C
            humidity = 40f + Random.nextFloat() * 30f,   // 40-70%
            co2 = 400f + Random.nextFloat() * 400f,      // 400-800ppm
            pm25 = 5f + Random.nextFloat() * 20f,        // 5-25μg/m³
            timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        )
    }

    /**
     * 生成模拟历史数据
     */
    private fun generateMockHistoryData(dataPoints: Int): List<EnvironmentData> {
        return (0 until dataPoints).map {
            EnvironmentData(
                temperature = 20f + Random.nextFloat() * 8f,
                humidity = 40f + Random.nextFloat() * 30f,
                co2 = 400f + Random.nextFloat() * 400f,
                pm25 = 5f + Random.nextFloat() * 20f,
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            )
        }
    }
}
