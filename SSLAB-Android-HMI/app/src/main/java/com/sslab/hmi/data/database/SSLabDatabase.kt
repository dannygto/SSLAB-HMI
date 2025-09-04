package com.sslab.hmi.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sslab.hmi.data.model.*

/**
 * 类型转换器
 */
class Converters {
    
    @TypeConverter
    fun fromDeviceType(type: DeviceType): String = type.name
    
    @TypeConverter
    fun toDeviceType(type: String): DeviceType = 
        try { DeviceType.valueOf(type) } catch (e: Exception) { DeviceType.UNKNOWN }
    
    @TypeConverter
    fun fromDeviceStatus(status: DeviceStatus): String = status.name
    
    @TypeConverter
    fun toDeviceStatus(status: String): DeviceStatus = 
        try { DeviceStatus.valueOf(status) } catch (e: Exception) { DeviceStatus.OFFLINE }
    
    @TypeConverter
    fun fromStudentDeviceStatus(status: StudentDeviceStatus): String = status.name
    
    @TypeConverter
    fun toStudentDeviceStatus(status: String): StudentDeviceStatus = 
        try { StudentDeviceStatus.valueOf(status) } catch (e: Exception) { StudentDeviceStatus.NORMAL }
    
    @TypeConverter
    fun fromLiftDirection(direction: LiftDirection): String = direction.name
    
    @TypeConverter
    fun toLiftDirection(direction: String): LiftDirection = 
        try { LiftDirection.valueOf(direction) } catch (e: Exception) { LiftDirection.STOP }
    
    @TypeConverter
    fun fromControlType(type: ControlType): String = type.name
    
    @TypeConverter
    fun toControlType(type: String): ControlType = 
        try { ControlType.valueOf(type) } catch (e: Exception) { ControlType.SWITCH }
}

/**
 * SSLAB HMI 数据库
 */
@Database(
    entities = [
        Device::class,
        TeachingPower::class,
        StudentPowerGroup::class,
        StudentDevice::class,
        EnvironmentData::class,
        LiftControl::class,
        DeviceControl::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SSLabDatabase : RoomDatabase() {
    
    abstract fun deviceDao(): DeviceDao
    abstract fun teachingPowerDao(): TeachingPowerDao
    abstract fun studentPowerGroupDao(): StudentPowerGroupDao
    abstract fun studentDeviceDao(): StudentDeviceDao
    abstract fun environmentDataDao(): EnvironmentDataDao
    abstract fun liftControlDao(): LiftControlDao
    abstract fun deviceControlDao(): DeviceControlDao
    
    companion object {
        private const val DATABASE_NAME = "sslab_database"
        
        @Volatile
        private var INSTANCE: SSLabDatabase? = null
        
        fun getDatabase(context: Context): SSLabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SSLabDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
