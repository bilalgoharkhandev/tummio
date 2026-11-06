package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FeedingType {
    FORMULA,
    EXPRESSED_BREAST_MILK,
    DIRECT_BREASTFEEDING,
    MIXED_BOTTLE
}

enum class NursingBreast {
    LEFT, RIGHT, BOTH
}

@Entity(tableName = "feeding_entries")
data class FeedingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val babyId: Long,
    val timestamp: Long, // Start time of feed
    val endTime: Long? = null,
    val type: FeedingType,
    // Bottle specific (mL)
    val preparedMl: Double = 0.0,
    val remainingMl: Double = 0.0,
    val consumedMl: Double = 0.0, // Calculated: prepared - remaining for bottle
    val formulaBrand: String = "",
    // Direct Nursing specific (minutes)
    val nursingLeftMinutes: Int = 0,
    val nursingRightMinutes: Int = 0,
    val lastBreastUsed: NursingBreast? = null,
    val caregiver: String = "",
    val notes: String = ""
)
