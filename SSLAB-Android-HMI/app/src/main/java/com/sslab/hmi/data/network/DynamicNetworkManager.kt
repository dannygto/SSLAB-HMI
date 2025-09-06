package com.sslab.hmi.data.network

import com.google.gson.Gson
import com.sslab.hmi.data.network.InteractiveTeachingApiService
import com.sslab.hmi.data.preferences.ServerConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 动态网络配置管理器
 * 支持运行时修改服务器地址
 */
@Singleton
class DynamicNetworkManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val serverConfigManager: ServerConfigManager
) {
    private var _currentRetrofit: Retrofit? = null
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    /**
     * 获取当前Retrofit实例
     */
    fun getRetrofit(): Retrofit {
        val currentBaseUrl = serverConfigManager.getBaseUrl()
        
        // 如果Retrofit实例不存在或者baseUrl已改变，重新创建
        if (_currentRetrofit == null || _currentRetrofit!!.baseUrl().toString() != currentBaseUrl) {
            _currentRetrofit = createRetrofit(currentBaseUrl)
        }
        
        return _currentRetrofit!!
    }
    
    /**
     * 获取API服务实例
     */
    inline fun <reified T> getApiService(serviceClass: Class<T>): T {
        return getRetrofit().create(serviceClass)
    }
    
    /**
     * 更新服务器配置并重新创建网络连接
     */
    fun updateServerConfig(serverUrl: String) {
        serverConfigManager.setServerUrl(serverUrl)
        _currentRetrofit = null // 强制重新创建
        _isConnected.value = false
    }
    
    /**
     * 测试连接
     */
    suspend fun testConnection(serverUrl: String? = null): Boolean {
        return try {
            val retrofit = if (serverUrl != null) {
                createRetrofit(if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/")
            } else {
                getRetrofit()
            }
            
            val apiService = retrofit.create(InteractiveTeachingApiService::class.java)
            // 执行一个简单的测试请求 - 尝试获取当前会话状态
            val response = apiService.getCurrentSession()
            val isSuccess = response.isSuccessful
            _isConnected.value = isSuccess
            isSuccess
        } catch (e: Exception) {
            _isConnected.value = false
            false
        }
    }
    
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
