package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "growth_entries")
data class GrowthEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val babyId: Long,
    val timestamp: Long,
    val weightKg: Double? = null,
    val lengthCm: Double? = null,
    val headCircumferenceCm: Double? = null,
    val isRecumbent: Boolean = true, // Recumbent length for <2 years, standing height for older
    val measurementSource: String = "Home", // Home, Clinic, Pediatrician
    val notes: String = ""
)

enum class DiaperType {
    WET, DIRTY, BOTH, DRY
}

enum class StoolConsistency {
    WATERY, LOOSE, SOFT, FORMED, HARD, PELLET_LIKE
}

@Entity(tableName = "diaper_entries")
data class DiaperEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val babyId: Long,
    val timestamp: Long,
    val type: DiaperType,
    val stoolColor: String = "", // e.g. Yellow, Mustard, Green, Brown, Black
    val stoolConsistency: StoolConsistency? = null,
    val amount: String = "Normal", // Light, Normal, Heavy
    val hasMucusOrBlood: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "sleep_entries")
data class SleepEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val babyId: Long,
    val startTime: Long,
    val endTime: Long,
    val isNap: Boolean = true,
    val quality: String = "Good", // Restful, Fussy, Interrupted
    val notes: String = ""
) {
    val durationMinutes: Long
        get() = ((endTime - startTime).coerceAtLeast(0)) / (1000 * 60)
}

@Entity(tableName = "symptom_entries")
data class SymptomEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val babyId: Long,
    val timestamp: Long,
    val symptomType: String, // Gas, Spit-up, Vomiting, Fussiness, Crying, Refusal, Skin Rash, etc.
    val severity: String = "Mild", // Mild, Moderate, Severe
    val formulaBrand: String = "",
    val notes: String = ""
)

@Entity(tableName = "pumping_entries")
data class PumpingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val babyId: Long,
    val timestamp: Long,
    val leftOutputMl: Double = 0.0,
    val rightOutputMl: Double = 0.0,
    val totalMl: Double = 0.0,
    val durationMinutes: Int = 15,
    val storageLocation: String = "Fridge", // Fridge, Freezer, Used Immediately
    val notes: String = ""
)

@Entity(tableName = "formula_products")
data class FormulaProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val manufacturer: String,
    val productName: String,
    val stage: String = "Stage 1", // Infant (0-6m), Stage 2 (6-12m), etc.
    val tinSizeGrams: Double = 800.0,
    val price: Double = 0.0,
    val currency: String = "$",
    val waterQuantityPerUnitMl: Double = 30.0, // e.g. 30mL water
    val scoopsPerUnit: Double = 1.0,           // per 1 scoop
    val scoopMassGrams: Double = 4.3,          // grams per level scoop
    val dateOpenedTimestamp: Long? = null,
    val expiryDateTimestamp: Long? = null,
    val estimatedPowderRemainingGrams: Double = 800.0,
    val isActive: Boolean = true
)

@Entity(tableName = "bottle_safety_entries")
data class BottleSafetyEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val babyId: Long,
    val preparedTimestamp: Long,
    val feedingStartedTimestamp: Long? = null,
    val isRefrigerated: Boolean = false,
    val isDiscarded: Boolean = false,
    val discardedTimestamp: Long? = null,
    val notes: String = ""
)
