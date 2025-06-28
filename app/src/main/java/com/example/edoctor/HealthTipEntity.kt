package com.example.edoctor



import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_tips")
data class HealthTipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String? = null // optional for future use
)
