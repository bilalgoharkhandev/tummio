package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        BabyProfile::class,
        FeedingEntry::class,
        GrowthEntry::class,
        DiaperEntry::class,
        SleepEntry::class,
        SymptomEntry::class,
        PumpingEntry::class,
        FormulaProduct::class,
        BottleSafetyEntry::class,
        ChildMeasurement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao
    abstract fun feedingDao(): FeedingDao
    abstract fun growthDao(): GrowthDao
    abstract fun childMeasurementDao(): ChildMeasurementDao
    abstract fun diaperDao(): DiaperDao
    abstract fun sleepDao(): SleepDao
    abstract fun symptomDao(): SymptomDao
    abstract fun pumpingDao(): PumpingDao
    abstract fun formulaDao(): FormulaDao
    abstract fun bottleSafetyDao(): BottleSafetyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "littleroutine_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
