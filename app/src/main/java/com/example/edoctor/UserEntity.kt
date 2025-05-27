package com.example.edoctor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val gender: String,
    val role: String ,
    val dob: String? = null,
    val address: String? = null,
    val bloodGroup: String? = null,
    val emergencyContact: String? = null
)