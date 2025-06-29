package com.example.edoctor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.edoctor.dao.AvailabilityDao
import com.example.edoctor.AppointmentDao
import com.example.edoctor.UserDao
@Database(
    entities = [UserEntity::class, AppointmentEntity::class, AvailabilityEntity::class, HealthTipEntity::class],
    version = 2, // 🔼 increase the version number
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun availabilityDao(): AvailabilityDao
    abstract fun userDao(): UserDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun healthTipDao(): HealthTipDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edoctor_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}


