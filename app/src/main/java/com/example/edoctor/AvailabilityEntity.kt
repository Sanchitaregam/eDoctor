package com.example.edoctor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "availability")
data class AvailabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorId: Int,
    val days: String,
    val fromTime: String,
    val toTime: String
)
