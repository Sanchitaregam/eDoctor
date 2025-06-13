package com.example.edoctor

import androidx.room.*
import com.example.edoctor.AppointmentEntity

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments WHERE patientId = :patientId")
    suspend fun getAppointmentsByPatientId(patientId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId")
    suspend fun getAppointmentsForDoctor(doctorId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND patientId = :patientId")
    suspend fun getAppointmentsByDoctorAndPatient(doctorId: Int, patientId: Int): List<AppointmentEntity>
}

