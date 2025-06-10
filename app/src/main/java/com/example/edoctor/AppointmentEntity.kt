package com.example.edoctor


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorId: Int,
    val patientName: String,
    val date: String,  // e.g., "2025-06-10"
    val time: String,  // e.g., "10:30 AM"
    val notes: String? = null
)
