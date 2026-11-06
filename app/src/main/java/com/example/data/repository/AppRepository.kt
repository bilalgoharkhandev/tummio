package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    val babyDao = db.babyDao()
    val feedingDao = db.feedingDao()
    val growthDao = db.growthDao()
    val diaperDao = db.diaperDao()
    val sleepDao = db.sleepDao()
    val symptomDao = db.symptomDao()
    val pumpingDao = db.pumpingDao()
    val formulaDao = db.formulaDao()
    val bottleSafetyDao = db.bottleSafetyDao()

    // Babies
    val allBabies: Flow<List<BabyProfile>> = babyDao.getAllBabies()
    val defaultBaby: Flow<BabyProfile?> = babyDao.getDefaultBaby()
    suspend fun insertBaby(baby: BabyProfile): Long = babyDao.insertBaby(baby)
    suspend fun updateBaby(baby: BabyProfile) = babyDao.updateBaby(baby)
    suspend fun deleteBaby(baby: BabyProfile) = babyDao.deleteBaby(baby)
    suspend fun setDefaultBaby(id: Long) {
        babyDao.clearDefaultFlags()
        babyDao.setDefaultBaby(id)
    }

    // Feeding
    fun getFeedings(babyId: Long): Flow<List<FeedingEntry>> = feedingDao.getFeedingsForBaby(babyId)
    fun getFeedingsSince(babyId: Long, since: Long): Flow<List<FeedingEntry>> = feedingDao.getFeedingsSince(babyId, since)
    fun getLastFeeding(babyId: Long): Flow<FeedingEntry?> = feedingDao.getLastFeeding(babyId)
    suspend fun insertFeeding(feeding: FeedingEntry): Long = feedingDao.insertFeeding(feeding)
    suspend fun updateFeeding(feeding: FeedingEntry) = feedingDao.updateFeeding(feeding)
    suspend fun deleteFeeding(feeding: FeedingEntry) = feedingDao.deleteFeeding(feeding)

    val childMeasurementDao = db.childMeasurementDao()

    // Growth & Child Measurements
    fun getGrowth(babyId: Long): Flow<List<GrowthEntry>> = growthDao.getGrowthForBaby(babyId)
    fun getLatestGrowth(babyId: Long): Flow<GrowthEntry?> = growthDao.getLatestGrowth(babyId)
    suspend fun insertGrowth(growth: GrowthEntry): Long = growthDao.insertGrowth(growth)
    suspend fun deleteGrowth(growth: GrowthEntry) = growthDao.deleteGrowth(growth)

    fun getChildMeasurements(childId: Long): Flow<List<ChildMeasurement>> = childMeasurementDao.getMeasurementsForChild(childId)
    fun getLatestChildMeasurement(childId: Long): Flow<ChildMeasurement?> = childMeasurementDao.getLatestMeasurement(childId)
    suspend fun insertChildMeasurement(measurement: ChildMeasurement): Long = childMeasurementDao.insertMeasurement(measurement)
    suspend fun deleteChildMeasurement(measurement: ChildMeasurement) = childMeasurementDao.deleteMeasurement(measurement)

    // Diaper
    fun getDiapers(babyId: Long): Flow<List<DiaperEntry>> = diaperDao.getDiapersForBaby(babyId)
    fun getDiapersSince(babyId: Long, since: Long): Flow<List<DiaperEntry>> = diaperDao.getDiapersSince(babyId, since)
    suspend fun insertDiaper(diaper: DiaperEntry): Long = diaperDao.insertDiaper(diaper)
    suspend fun deleteDiaper(diaper: DiaperEntry) = diaperDao.deleteDiaper(diaper)

    // Sleep
    fun getSleep(babyId: Long): Flow<List<SleepEntry>> = sleepDao.getSleepForBaby(babyId)
    fun getSleepSince(babyId: Long, since: Long): Flow<List<SleepEntry>> = sleepDao.getSleepSince(babyId, since)
    suspend fun insertSleep(sleep: SleepEntry): Long = sleepDao.insertSleep(sleep)
    suspend fun deleteSleep(sleep: SleepEntry) = sleepDao.deleteSleep(sleep)

    // Symptoms
    fun getSymptoms(babyId: Long): Flow<List<SymptomEntry>> = symptomDao.getSymptomsForBaby(babyId)
    suspend fun insertSymptom(symptom: SymptomEntry): Long = symptomDao.insertSymptom(symptom)
    suspend fun deleteSymptom(symptom: SymptomEntry) = symptomDao.deleteSymptom(symptom)

    // Pumping
    fun getPumping(babyId: Long): Flow<List<PumpingEntry>> = pumpingDao.getPumpingForBaby(babyId)
    suspend fun insertPumping(pumping: PumpingEntry): Long = pumpingDao.insertPumping(pumping)
    suspend fun deletePumping(pumping: PumpingEntry) = pumpingDao.deletePumping(pumping)

    // Formula
    val allFormulas: Flow<List<FormulaProduct>> = formulaDao.getAllFormulas()
    val activeFormula: Flow<FormulaProduct?> = formulaDao.getActiveFormula()
    suspend fun insertFormula(formula: FormulaProduct): Long = formulaDao.insertFormula(formula)
    suspend fun updateFormula(formula: FormulaProduct) = formulaDao.updateFormula(formula)
    suspend fun deleteFormula(formula: FormulaProduct) = formulaDao.deleteFormula(formula)

    // Bottle Safety
    fun getActiveBottles(babyId: Long): Flow<List<BottleSafetyEntry>> = bottleSafetyDao.getActiveBottles(babyId)
    suspend fun insertBottleSafety(bottle: BottleSafetyEntry): Long = bottleSafetyDao.insertBottle(bottle)
    suspend fun updateBottleSafety(bottle: BottleSafetyEntry) = bottleSafetyDao.updateBottle(bottle)
    suspend fun deleteBottleSafety(bottle: BottleSafetyEntry) = bottleSafetyDao.deleteBottle(bottle)
}
