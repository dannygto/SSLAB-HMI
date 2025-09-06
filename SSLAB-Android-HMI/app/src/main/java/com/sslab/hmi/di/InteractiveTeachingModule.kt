package com.sslab.hmi.di

import com.sslab.hmi.data.repository.InteractiveTeachingRepository
import com.sslab.hmi.data.repository.InteractiveTeachingRepositorySimple
import com.sslab.hmi.data.network.InteractiveTeachingApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 互动教学模块的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
object InteractiveTeachingModule {
    
    @Provides
    @Singleton
    fun provideInteractiveTeachingRepository(): InteractiveTeachingRepository {
        return InteractiveTeachingRepository()
    }
    
    @Provides
    @Singleton
    fun provideInteractiveTeachingRepositorySimple(
        apiService: InteractiveTeachingApiService
    ): InteractiveTeachingRepositorySimple {
        return InteractiveTeachingRepositorySimple(apiService)
    }
}
