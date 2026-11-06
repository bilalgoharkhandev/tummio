package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {
    @Query("SELECT * FROM baby_profiles ORDER BY id ASC")
    fun getAllBabies(): Flow<List<BabyProfile>>

    @Query("SELECT * FROM baby_profiles WHERE isDefault = 1 LIMIT 1")
    fun getDefaultBaby(): Flow<BabyProfile?>

    @Query("SELECT * FROM baby_profiles WHERE id = :id")
    suspend fun getBabyById(id: Long): BabyProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaby(baby: BabyProfile): Long

    @Update
    suspend fun updateBaby(baby: BabyProfile)

    @Delete
    suspend fun deleteBaby(baby: BabyProfile)

    @Query("UPDATE baby_profiles SET isDefault = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE baby_profiles SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultBaby(id: Long)
}

@Dao
interface FeedingDao {
    @Query("SELECT * FROM feeding_entries WHERE babyId = :babyId ORDER BY timestamp DESC")
    fun getFeedingsForBaby(babyId: Long): Flow<List<FeedingEntry>>

    @Query("SELECT * FROM feeding_entries WHERE babyId = :babyId AND timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getFeedingsSince(babyId: Long, sinceTimestamp: Long): Flow<List<FeedingEntry>>

    @Query("SELECT * FROM feeding_entries WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 1")
    fun getLastFeeding(babyId: Long): Flow<FeedingEntry?>

    @Query("SELECT * FROM feeding_entries WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastFeedingSync(babyId: Long): FeedingEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeding(feeding: FeedingEntry): Long

    @Update
    suspend fun updateFeeding(feeding: FeedingEntry)

    @Delete
    suspend fun deleteFeeding(feeding: FeedingEntry)

    @Query("DELETE FROM feeding_entries WHERE babyId = :babyId")
    suspend fun deleteAllForBaby(babyId: Long)
}

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_entries WHERE babyId = :babyId ORDER BY timestamp ASC")
    fun getGrowthForBaby(babyId: Long): Flow<List<GrowthEntry>>

    @Query("SELECT * FROM growth_entries WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestGrowth(babyId: Long): Flow<GrowthEntry?>

    @Query("SELECT * FROM growth_entries WHERE babyId = :babyId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestGrowthSync(babyId: Long): GrowthEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowth(growth: GrowthEntry): Long

    @Update
    suspend fun updateGrowth(growth: GrowthEntry)

    @Delete
    suspend fun deleteGrowth(growth: GrowthEntry)
}

@Dao
interface DiaperDao {
    @Query("SELECT * FROM diaper_entries WHERE babyId = :babyId ORDER BY timestamp DESC")
    fun getDiapersForBaby(babyId: Long): Flow<List<DiaperEntry>>

    @Query("SELECT * FROM diaper_entries WHERE babyId = :babyId AND timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getDiapersSince(babyId: Long, sinceTimestamp: Long): Flow<List<DiaperEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaper(diaper: DiaperEntry): Long

    @Delete
    suspend fun deleteDiaper(diaper: DiaperEntry)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_entries WHERE babyId = :babyId ORDER BY startTime DESC")
    fun getSleepForBaby(babyId: Long): Flow<List<SleepEntry>>

    @Query("SELECT * FROM sleep_entries WHERE babyId = :babyId AND startTime >= :sinceTimestamp ORDER BY startTime DESC")
    fun getSleepSince(babyId: Long, sinceTimestamp: Long): Flow<List<SleepEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleep(sleep: SleepEntry): Long

    @Delete
    suspend fun deleteSleep(sleep: SleepEntry)
}

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptom_entries WHERE babyId = :babyId ORDER BY timestamp DESC")
    fun getSymptomsForBaby(babyId: Long): Flow<List<SymptomEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomEntry): Long

    @Delete
    suspend fun deleteSymptom(symptom: SymptomEntry)
}

@Dao
interface PumpingDao {
    @Query("SELECT * FROM pumping_entries WHERE babyId = :babyId ORDER BY timestamp DESC")
    fun getPumpingForBaby(babyId: Long): Flow<List<PumpingEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPumping(pumping: PumpingEntry): Long

    @Delete
    suspend fun deletePumping(pumping: PumpingEntry)
}

@Dao
interface FormulaDao {
    @Query("SELECT * FROM formula_products ORDER BY isActive DESC, id DESC")
    fun getAllFormulas(): Flow<List<FormulaProduct>>

    @Query("SELECT * FROM formula_products WHERE isActive = 1 LIMIT 1")
    fun getActiveFormula(): Flow<FormulaProduct?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: FormulaProduct): Long

    @Update
    suspend fun updateFormula(formula: FormulaProduct)

    @Delete
    suspend fun deleteFormula(formula: FormulaProduct)
}

@Dao
interface BottleSafetyDao {
    @Query("SELECT * FROM bottle_safety_entries WHERE babyId = :babyId AND isDiscarded = 0 ORDER BY preparedTimestamp DESC")
    fun getActiveBottles(babyId: Long): Flow<List<BottleSafetyEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBottle(bottle: BottleSafetyEntry): Long

    @Update
    suspend fun updateBottle(bottle: BottleSafetyEntry)

    @Delete
    suspend fun deleteBottle(bottle: BottleSafetyEntry)
}
