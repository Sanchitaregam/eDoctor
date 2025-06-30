package com.example.edoctor

import androidx.room.*

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE LOWER(role) = 'doctor'")
    suspend fun getAllDoctors(): List<UserEntity>

    @Query("SELECT * FROM users WHERE LOWER(role) = 'patient'")
    suspend fun getAllPatients(): List<UserEntity>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("UPDATE users SET isApproved = :isApproved WHERE id = :doctorId")
    suspend fun approveDoctor(doctorId: Int, isApproved: Boolean)

}
