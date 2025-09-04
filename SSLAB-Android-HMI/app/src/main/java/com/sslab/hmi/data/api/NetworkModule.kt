package com.sslab.hmi.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 网络模块配置
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    /**
     * 基础URL限定符
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BaseUrl
    
    /**
     * 模拟器URL限定符
     */
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SimulatorUrl
    
    @Provides
    @BaseUrl
    fun provideBaseUrl(): String = "http://192.168.1.100:8080/"
    
    @Provides
    @SimulatorUrl
    fun provideSimulatorUrl(): String = "http://localhost:3000/"
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
    
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
    
    @Provides
    @Singleton
    fun provideRetrofit(
        @SimulatorUrl baseUrl: String,
        gson: Gson,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideDeviceApi(retrofit: Retrofit): DeviceApi {
        return retrofit.create(DeviceApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideTeachingPowerApi(retrofit: Retrofit): TeachingPowerApi {
        return retrofit.create(TeachingPowerApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideEnvironmentApi(retrofit: Retrofit): EnvironmentApi {
        return retrofit.create(EnvironmentApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideLiftControlApi(retrofit: Retrofit): LiftControlApi {
        return retrofit.create(LiftControlApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDeviceControlApi(retrofit: Retrofit): DeviceControlApi {
        return retrofit.create(DeviceControlApi::class.java)
    }
}
