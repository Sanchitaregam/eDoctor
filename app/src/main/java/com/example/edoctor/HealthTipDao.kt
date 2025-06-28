package com.example.edoctor



import androidx.room.*
import com.example.edoctor.HealthTipEntity

@Dao
interface HealthTipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: HealthTipEntity)

    @Query("SELECT * FROM health_tips ORDER BY id DESC")
    suspend fun getAllTips(): List<HealthTipEntity>

    @Delete
    suspend fun deleteTip(tip: HealthTipEntity)
}
