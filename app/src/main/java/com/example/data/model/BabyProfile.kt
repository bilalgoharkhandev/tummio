package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SexCategory {
    BOY, GIRL
}

enum class FeedingMethod {
    FORMULA, BREASTFEEDING, EXPRESSED_BREAST_MILK, MIXED
}

@Entity(tableName = "baby_profiles")
data class BabyProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val birthTimestamp: Long,
    val sex: SexCategory,
    val birthWeightKg: Double? = null,
    val birthLengthCm: Double? = null,
    val birthHeadCircumferenceCm: Double? = null,
    val gestationalAgeWeeks: Int? = null, // e.g. 38
    val gestationalAgeDays: Int? = null,  // e.g. 4
    val primaryFeedingMethod: FeedingMethod = FeedingMethod.MIXED,
    val reminderIntervalMinutes: Int = 180, // Default 3 hours (180 mins)
    val preferredUnits: String = "METRIC", // METRIC (mL, kg, cm) or IMPERIAL (oz, lb, in)
    val photoUri: String? = null,
    val isDefault: Boolean = false,
    val notes: String = ""
)
