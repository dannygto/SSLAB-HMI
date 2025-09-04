package com.sslab.hmi.di

import android.content.Context
import com.sslab.hmi.data.discovery.DeviceDiscoveryService
import com.sslab.hmi.data.repository.ClassroomConfigRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClassroomModule {
    
    @Provides
    @Singleton
    fun provideDeviceDiscoveryService(): DeviceDiscoveryService {
        return DeviceDiscoveryService()
    }
    
    @Provides
    @Singleton
    fun provideClassroomConfigRepository(
        @ApplicationContext context: Context,
        deviceDiscoveryService: DeviceDiscoveryService
    ): ClassroomConfigRepository {
        return ClassroomConfigRepository(context, deviceDiscoveryService)
    }
}
