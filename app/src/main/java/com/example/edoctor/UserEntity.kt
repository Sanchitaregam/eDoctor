package com.example.edoctor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var password: String = "",
    var gender: String = "",
    var role: String = "", // "doctor", "patient", or "admin"

    // Common optional fields
    var dob: String? = null,
    var address: String? = null,
    var bloodGroup: String? = null,
    var emergencyContact: String? = null,
    var experience: String? = null,

    // Doctor-specific fields (only used if role == "doctor")
    var specialization: String? = null,
    var rating: Float = 0f,
    var ratingCount: Int = 0
)
