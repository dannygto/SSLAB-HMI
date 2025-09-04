package com.sslab.hmi.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sslab.hmi.data.network.SSLabApiService
import com.sslab.hmi.data.network.WebSocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络模块依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
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
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * 提供Retrofit实例
     * 配置为连接到SSLAB设备模拟器服务器
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.0.145:8080/") // SSLAB设备模拟器服务器地址
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
}
