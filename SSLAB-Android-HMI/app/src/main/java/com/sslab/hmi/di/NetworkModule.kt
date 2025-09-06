package com.sslab.hmi.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sslab.hmi.data.network.SSLabApiService
import com.sslab.hmi.data.network.InteractiveTeachingApiService
import com.sslab.hmi.data.network.WebSocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 网络模块依赖注入
 * 支持HTTP和HTTPS协议，自动处理SSL证书信任
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // 服务器配置
    private const val SERVER_IP = "192.168.0.145"
    private const val HTTP_PORT = 8080
    private const val HTTPS_PORT = 8443
    
    // 协议选择：开发环境用HTTP，生产环境用HTTPS
    private const val USE_HTTPS = false
    
    private val BASE_URL = if (USE_HTTPS) {
        "https://$SERVER_IP:$HTTPS_PORT/"
    } else {
        "http://$SERVER_IP:$HTTP_PORT/"
    }
    
    /**
     * 提供Gson实例
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
    }
    
    /**
     * 提供OkHttpClient
     * 支持HTTP和HTTPS，开发环境信任自签名证书
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        // 如果使用HTTPS，配置SSL信任
        if (USE_HTTPS) {
            try {
                // 创建信任所有证书的TrustManager（仅开发环境）
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })
                
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, SecureRandom())
                
                builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return builder.build()
    }
    
    /**
     * 提供Retrofit实例
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * 提供API服务
     */
    @Provides
    @Singleton
    fun provideSSLabApiService(retrofit: Retrofit): SSLabApiService {
        return retrofit.create(SSLabApiService::class.java)
    }

    /**
     * 提供教学电源API服务
     */
    @Provides
    @Singleton
    fun provideTeachingPowerApi(retrofit: Retrofit): com.sslab.hmi.data.api.TeachingPowerApi {
        return retrofit.create(com.sslab.hmi.data.api.TeachingPowerApi::class.java)
    }

    /**
     * 提供互动教学API服务
     */
    @Provides
    @Singleton
    fun provideInteractiveTeachingApiService(retrofit: Retrofit): InteractiveTeachingApiService {
        return retrofit.create(InteractiveTeachingApiService::class.java)
    }
}
