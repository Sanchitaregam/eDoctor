package com.example.edoctor.data.dao

import androidx.room.*
import com.example.edoctor.data.entities.AvailabilityEntity

@Dao
interface AvailabilityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvailability(availability: AvailabilityEntity)

    @Query("SELECT * FROM availability WHERE doctorId = :doctorId")
    suspend fun getAvailabilityForDoctor(doctorId: Int): List<AvailabilityEntity>
}
