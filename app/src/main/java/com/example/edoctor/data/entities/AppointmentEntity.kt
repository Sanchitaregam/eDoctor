package com.example.edoctor.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorId: Int,
    val patientId: Int,
    val patientName: String,
    val doctorName: String,
    val date: String,
    val time: String,
    val notes: String? = null  // <- this is the correct way
)
