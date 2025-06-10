package com.example.edoctor


import androidx.room.*

@Dao
interface AppointmentDao {

    @Insert
    suspend fun addAppointment(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY date, time")
    suspend fun getAppointmentsForDoctor(doctorId: Int): List<AppointmentEntity>

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)
}
