

package com.example.edoctor.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.edoctor.data.entities.AvailabilityEntity

@Dao
interface AvailabilityDao {
    @Insert
    suspend fun insertAvailability(availability: AvailabilityEntity)

    @Query("SELECT * FROM availability WHERE doctorId = :doctorId")
    suspend fun getAvailabilityForDoctor(doctorId: Int): List<AvailabilityEntity>
}
