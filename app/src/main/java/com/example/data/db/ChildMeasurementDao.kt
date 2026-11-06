package com.example.data.db

import androidx.room.*
import com.example.data.model.ChildMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildMeasurementDao {

    @Query("SELECT * FROM child_measurements WHERE childId = :childId ORDER BY timestamp ASC")
    fun getMeasurementsForChild(childId: Long): Flow<List<ChildMeasurement>>

    @Query("SELECT * FROM child_measurements WHERE childId = :childId ORDER BY timestamp DESC")
    fun getMeasurementsForChildDesc(childId: Long): Flow<List<ChildMeasurement>>

    @Query("SELECT * FROM child_measurements WHERE childId = :childId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMeasurement(childId: Long): Flow<ChildMeasurement?>

    @Query("SELECT * FROM child_measurements WHERE childId = :childId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMeasurementSync(childId: Long): ChildMeasurement?

    @Query("SELECT * FROM child_measurements WHERE childId = :childId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getMeasurementsInRange(childId: Long, startTime: Long, endTime: Long): Flow<List<ChildMeasurement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: ChildMeasurement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<ChildMeasurement>): List<Long>

    @Update
    suspend fun updateMeasurement(measurement: ChildMeasurement)

    @Delete
    suspend fun deleteMeasurement(measurement: ChildMeasurement)

    @Query("DELETE FROM child_measurements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM child_measurements WHERE childId = :childId")
    suspend fun deleteAllForChild(childId: Long)
}
