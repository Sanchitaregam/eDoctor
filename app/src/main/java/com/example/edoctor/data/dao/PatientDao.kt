package com.example.edoctor.data.dao

import androidx.room.*
import com.example.edoctor.data.entities.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginPatient(email: String, password: String): PatientEntity?
    
    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Int): PatientEntity?
    
    @Query("SELECT * FROM patients WHERE email = :email")
    suspend fun getPatientByEmail(email: String): PatientEntity?
    
    @Query("SELECT * FROM patients")
    suspend fun getAllPatients(): List<PatientEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long
    
    @Update
    suspend fun updatePatient(patient: PatientEntity)
    
    @Delete
    suspend fun deletePatient(patient: PatientEntity)
    
    @Query("UPDATE patients SET knownConditions = :conditions, allergies = :allergies, currentMedications = :medications, pastSurgeries = :surgeries, familyHistory = :familyHistory WHERE id = :patientId")
    suspend fun updateMedicalHistory(
        patientId: Int,
        conditions: String?,
        allergies: String?,
        medications: String?,
        surgeries: String?,
        familyHistory: String?
    )
} 