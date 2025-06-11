package com.example.edoctor

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.edoctor.dao.AvailabilityDao
import com.example.edoctor.AppointmentDao
import com.example.edoctor.UserDao

@Database(
    entities = [
        UserEntity::class,
        AppointmentEntity::class,
        AvailabilityEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun availabilityDao(): AvailabilityDao
}
