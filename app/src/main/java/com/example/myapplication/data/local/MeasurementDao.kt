package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface MeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: MeasurementEntity): Long

    @Update
    suspend fun updateMeasurement(measurement: MeasurementEntity): Int

    @Delete
    suspend fun deleteMeasurement(measurement: MeasurementEntity): Int

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): MeasurementEntity?

    @Query(
        """
            SELECT * FROM measurements
            WHERE timestamp BETWEEN :startMillis AND :endMillis
            ORDER BY timestamp DESC
        """
    )
    fun getMeasurementsForRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<MeasurementEntity>>

    @Query(
        """
            SELECT * FROM measurements
            WHERE type = :type AND timestamp BETWEEN :startMillis AND :endMillis
            ORDER BY timestamp DESC
        """
    )
    fun getMeasurementsForTypeAndRange(
        type: String,
        startMillis: Long,
        endMillis: Long
    ): Flow<List<MeasurementEntity>>

    @Query(
        """
            SELECT * FROM measurements
            ORDER BY timestamp DESC
            LIMIT :limit
        """
    )
    fun getLatestMeasurements(limit: Int): Flow<List<MeasurementEntity>>
}

