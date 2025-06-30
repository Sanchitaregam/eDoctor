package com.example.edoctor.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var password: String = "",
    var gender: String = "",
    var dob: String? = null,
    var address: String? = null,
    var isApproved: Boolean = false,
    var specialization: String? = null,
    var experience: Int? = null
) 