package com.example.edoctor.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var password: String = "",
    var gender: String = "",
    var dob: String? = null,
    var address: String? = null,
    var bloodGroup: String? = null,
    
    // Medical history fields
    var knownConditions: String? = null,
    var allergies: String? = null,
    var currentMedications: String? = null,
    var pastSurgeries: String? = null,
    var familyHistory: String? = null,
) 