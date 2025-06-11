package com.example.edoctor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var email: String,
    var phone: String,
    var password: String,
    var gender: String,
    var role: String,
    var dob: String? = null,
    var address: String? = null,
    var bloodGroup: String? = null,
    var emergencyContact: String? = null,
    var experience: String? = null
)
