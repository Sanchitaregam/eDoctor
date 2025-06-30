package com.example.edoctor.data.dao

import androidx.room.*
import com.example.edoctor.data.entities.AdminEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    @Query("SELECT * FROM admins WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginAdmin(email: String, password: String): AdminEntity?
    
    @Query("SELECT * FROM admins WHERE id = :id")
    suspend fun getAdminById(id: Int): AdminEntity?
    
    @Query("SELECT * FROM admins WHERE email = :email")
    suspend fun getAdminByEmail(email: String): AdminEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminEntity): Long
    
    @Update
    suspend fun updateAdmin(admin: AdminEntity)
    
    @Delete
    suspend fun deleteAdmin(admin: AdminEntity)
    
    @Query("SELECT * FROM admins")
    suspend fun getAllAdmins(): List<AdminEntity>
} 