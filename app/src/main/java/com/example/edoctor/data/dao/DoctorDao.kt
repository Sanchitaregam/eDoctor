package com.example.edoctor.data.dao

import androidx.room.*
import com.example.edoctor.data.entities.DoctorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {
    @Query("SELECT * FROM doctors WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginDoctor(email: String, password: String): DoctorEntity?
    
    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: Int): DoctorEntity?
    
    @Query("SELECT * FROM doctors WHERE email = :email")
    suspend fun getDoctorByEmail(email: String): DoctorEntity?
    
    @Query("SELECT * FROM doctors WHERE isApproved = 1")
    suspend fun getApprovedDoctors(): List<DoctorEntity>
    
    @Query("SELECT * FROM doctors")
    suspend fun getAllDoctors(): List<DoctorEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: DoctorEntity): Long
    
    @Update
    suspend fun updateDoctor(doctor: DoctorEntity)
    
    @Delete
    suspend fun deleteDoctor(doctor: DoctorEntity)
    
    @Query("UPDATE doctors SET isApproved = :isApproved WHERE id = :doctorId")
    suspend fun updateApprovalStatus(doctorId: Int, isApproved: Boolean)
} 