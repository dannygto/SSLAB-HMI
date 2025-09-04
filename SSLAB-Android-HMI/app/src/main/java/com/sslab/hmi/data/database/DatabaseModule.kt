package com.sslab.hmi.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库模块配置
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideSSLabDatabase(@ApplicationContext context: Context): SSLabDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SSLabDatabase::class.java,
            "sslab_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideDeviceDao(database: SSLabDatabase): DeviceDao {
        return database.deviceDao()
    }
    
    @Provides
    fun provideTeachingPowerDao(database: SSLabDatabase): TeachingPowerDao {
        return database.teachingPowerDao()
    }
    
    @Provides
    fun provideStudentPowerGroupDao(database: SSLabDatabase): StudentPowerGroupDao {
        return database.studentPowerGroupDao()
    }
    
    @Provides
    fun provideStudentDeviceDao(database: SSLabDatabase): StudentDeviceDao {
        return database.studentDeviceDao()
    }
    
    @Provides
    fun provideEnvironmentDataDao(database: SSLabDatabase): EnvironmentDataDao {
        return database.environmentDataDao()
    }
    
    @Provides
    fun provideLiftControlDao(database: SSLabDatabase): LiftControlDao {
        return database.liftControlDao()
    }
    
    @Provides
    fun provideDeviceControlDao(database: SSLabDatabase): DeviceControlDao {
        return database.deviceControlDao()
    }
}
