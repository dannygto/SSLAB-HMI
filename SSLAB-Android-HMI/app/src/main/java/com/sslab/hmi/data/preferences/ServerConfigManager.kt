package com.sslab.hmi.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务器配置管理器
 */
@Singleton
class ServerConfigManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREF_NAME = "server_config"
        private const val KEY_SERVER_URL = "server_url"
        
        // 智能选择默认服务器地址
        private val DEFAULT_SERVER_URL = run {
            val isEmulator = (android.os.Build.FINGERPRINT.startsWith("generic")
                    || android.os.Build.FINGERPRINT.contains("sdk")
                    || android.os.Build.FINGERPRINT.contains("emulator")
                    || android.os.Build.MODEL.contains("Emulator")
                    || android.os.Build.MODEL.contains("Android SDK")
                    || android.os.Build.DEVICE.contains("generic")
                    || android.os.Build.PRODUCT.contains("sdk")
                    || android.os.Build.PRODUCT.contains("emulator"))
            
            if (isEmulator) {
                "http://10.0.2.2:8080"  // 模拟器访问宿主机
            } else {
                "http://192.168.0.145:8080"  // 物理设备访问实际IP
            }
        }
    }
    
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    private val _serverUrl = MutableStateFlow(getServerUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()
    
    /**
     * 获取当前服务器URL
     */
    fun getServerUrl(): String {
        return preferences.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }
    
    /**
     * 设置服务器URL
     */
    fun setServerUrl(url: String) {
        preferences.edit()
            .putString(KEY_SERVER_URL, url)
            .apply()
        _serverUrl.value = url
    }
    
    /**
     * 获取基础URL（用于Retrofit）
     */
    fun getBaseUrl(): String {
        val url = getServerUrl()
        return if (url.endsWith("/")) url else "$url/"
    }
}
