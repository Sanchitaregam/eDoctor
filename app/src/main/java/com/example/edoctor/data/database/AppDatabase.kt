package com.example.edoctor.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.edoctor.data.dao.AppointmentDao
import com.example.edoctor.data.dao.AvailabilityDao
import com.example.edoctor.data.dao.DoctorDao
import com.example.edoctor.data.dao.PatientDao
import com.example.edoctor.data.dao.AdminDao
import com.example.edoctor.data.entities.AppointmentEntity
import com.example.edoctor.data.entities.AvailabilityEntity
import com.example.edoctor.data.entities.DoctorEntity
import com.example.edoctor.data.entities.PatientEntity
import com.example.edoctor.data.entities.AdminEntity

@Database(
    entities = [
        AppointmentEntity::class, 
        AvailabilityEntity::class,
        DoctorEntity::class,
        PatientEntity::class,
        AdminEntity::class
    ],
    version = 11, // 🔼 increase the version number
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun availabilityDao(): AvailabilityDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun doctorDao(): DoctorDao
    abstract fun patientDao(): PatientDao
    abstract fun adminDao(): AdminDao

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


