package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database schema for child growth measurements.
 * Captures weight, length/height, head circumference, and precalculated WHO percentiles & Z-scores.
 */
@Entity(
    tableName = "child_measurements",
    foreignKeys = [
        ForeignKey(
            entity = BabyProfile::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["childId"]),
        Index(value = ["timestamp"]),
        Index(value = ["childId", "timestamp"])
    ]
)
data class ChildMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val childId: Long,
    val timestamp: Long, // Epoch timestamp in milliseconds
    val ageInDays: Long, // Age of child in days at measurement time
    val weightKg: Double? = null,
    val lengthCm: Double? = null,
    val headCircumferenceCm: Double? = null,
    // Calculated WHO standards percentiles (0.0 to 100.0)
    val weightPercentile: Double? = null,
    val lengthPercentile: Double? = null,
    val headCircumferencePercentile: Double? = null,
    // Standard deviations (Z-scores) from WHO median
    val weightZScore: Double? = null,
    val lengthZScore: Double? = null,
    val headCircumferenceZScore: Double? = null,
    // Clinical measurement flags
    val isRecumbent: Boolean = true, // Recumbent length for <2 years, standing height for older
    val measurementSource: String = "Home", // "Home", "Clinic", "Pediatrician"
    val notes: String = ""
)
