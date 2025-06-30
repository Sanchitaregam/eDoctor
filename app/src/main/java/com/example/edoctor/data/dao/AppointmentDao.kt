package com.example.edoctor.data.dao

import androidx.room.*
import com.example.edoctor.data.entities.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments WHERE patientId = :patientId AND date >= date('now') ORDER BY date ASC, time ASC")
    suspend fun getUpcomingAppointmentsByPatientId(patientId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND date >= date('now') ORDER BY date ASC, time ASC")
    suspend fun getUpcomingAppointmentsForDoctor(doctorId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND patientId = :patientId AND date >= date('now') ORDER BY date ASC, time ASC")
    suspend fun getUpcomingAppointmentsByDoctorAndPatient(doctorId: Int, patientId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE date >= date('now') ORDER BY date ASC, time ASC")
    suspend fun getUpcomingAppointments(): List<AppointmentEntity>

    // Legacy methods for backward compatibility (keeping all appointments)
    @Query("SELECT * FROM appointments WHERE patientId = :patientId")
    suspend fun getAppointmentsByPatientId(patientId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId")
    suspend fun getAppointmentsForDoctor(doctorId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND patientId = :patientId")
    suspend fun getAppointmentsByDoctorAndPatient(doctorId: Int, patientId: Int): List<AppointmentEntity>

    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointments(): List<AppointmentEntity>
}

