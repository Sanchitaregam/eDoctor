package com.example.edoctor.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    var email: String = "",
    var phone: String = "",
    var password: String = "",
    var gender: String = "",
    var dob: String? = null,
    var address: String? = null
) 