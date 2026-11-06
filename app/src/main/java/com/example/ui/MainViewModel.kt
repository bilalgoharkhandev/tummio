package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.data.repository.PreferencesRepository
import com.example.guidelines.FeedingGuidelineCalculator
import com.example.guidelines.GuidelineComparison
import com.example.reminder.FeedingReminderScheduler
import com.example.reminder.ReminderKind
import com.example.reminder.SmartReminderManager
import com.example.who.GrowthAssessment
import com.example.who.WhoGrowthCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class DailySummary(
    val formulaConsumedMl: Double = 0.0,
    val ebmConsumedMl: Double = 0.0,
    val totalBottleIntakeMl: Double = 0.0,
    val bottleSessionsCount: Int = 0,
    val nursingSessionsCount: Int = 0,
    val nursingTotalMinutes: Int = 0,
    val averageBottleMl: Double = 0.0,
    val averageIntervalMinutes: Long = 0,
    val wetDiapersCount: Int = 0,
    val dirtyDiapersCount: Int = 0,
    val sleepTotalMinutes: Long = 0
)

data class NursingTimerState(
    val isRunning: Boolean = false,
    val activeBreast: NursingBreast? = null,
    val leftSeconds: Long = 0,
    val rightSeconds: Long = 0,
    val lastUsedBreast: NursingBreast? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    val preferences = PreferencesRepository(application)

    val allBabies = repository.allBabies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val selectedBabyId = preferences.selectedBabyId.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val isMetric = preferences.isMetric.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val useAap = preferences.useAap.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val themeMode = preferences.themeMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "LIGHT")

    val activeBaby: StateFlow<BabyProfile?> = combine(allBabies, selectedBabyId) { babies, selId ->
        if (babies.isEmpty()) null
        else babies.find { it.id == selId } ?: babies.find { it.isDefault } ?: babies.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current baby's records
    val feedings = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else repository.getFeedings(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastFeeding = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(null) else repository.getLastFeeding(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val growthList = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else repository.getGrowth(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestGrowth = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(null) else repository.getLatestGrowth(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val diapers = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else repository.getDiapers(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sleepList = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else repository.getSleep(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptoms = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else repository.getSymptoms(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val formulas = repository.allFormulas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeBottles = activeBaby.flatMapLatest { baby ->
        if (baby == null) flowOf(emptyList()) else repository.getActiveBottles(baby.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Nursing Timer State
    private val _nursingTimerState = MutableStateFlow(NursingTimerState())
    val nursingTimerState = _nursingTimerState.asStateFlow()
    private var timerJob: Job? = null

    // Real-time ticker for countdown and timestamps
    private val _currentTicker = MutableStateFlow(System.currentTimeMillis())
    val currentTicker = _currentTicker.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTicker.value = System.currentTimeMillis()
            }
        }
    }

    // Daily summary calculation (rolling 24-hour window)
    val dailySummary: StateFlow<DailySummary> = combine(
        feedings, diapers, sleepList, _currentTicker
    ) { feedList, diaperList, slpList, now ->
        val cutoff = now - TimeUnit.HOURS.toMillis(24)
        val recentFeeds = feedList.filter { it.timestamp >= cutoff }
        val recentDiapers = diaperList.filter { it.timestamp >= cutoff }
        val recentSleep = slpList.filter { it.startTime >= cutoff }

        val formulaMl = recentFeeds.filter { it.type == FeedingType.FORMULA || it.type == FeedingType.MIXED_BOTTLE }.sumOf { it.consumedMl }
        val ebmMl = recentFeeds.filter { it.type == FeedingType.EXPRESSED_BREAST_MILK }.sumOf { it.consumedMl }
        val totalBottleMl = formulaMl + ebmMl

        val bottleFeeds = recentFeeds.filter { it.type != FeedingType.DIRECT_BREASTFEEDING }
        val avgBottle = if (bottleFeeds.isNotEmpty()) totalBottleMl / bottleFeeds.size else 0.0

        val nursingFeeds = recentFeeds.filter { it.type == FeedingType.DIRECT_BREASTFEEDING }
        val nursingMins = nursingFeeds.sumOf { it.nursingLeftMinutes + it.nursingRightMinutes }

        // Interval between recent feeds
        val sorted = recentFeeds.sortedBy { it.timestamp }
        var totalInterval: Long = 0
        var intervalsCount = 0
        for (i in 0 until sorted.size - 1) {
            val diff = sorted[i + 1].timestamp - sorted[i].timestamp
            if (diff in 1..TimeUnit.HOURS.toMillis(12)) {
                totalInterval += diff
                intervalsCount++
            }
        }
        val avgIntervalMins = if (intervalsCount > 0) (totalInterval / intervalsCount) / (1000 * 60) else 0

        val wetCount = recentDiapers.count { it.type == DiaperType.WET || it.type == DiaperType.BOTH }
        val dirtyCount = recentDiapers.count { it.type == DiaperType.DIRTY || it.type == DiaperType.BOTH }
        val sleepMins = recentSleep.sumOf { it.durationMinutes }

        DailySummary(
            formulaConsumedMl = formulaMl,
            ebmConsumedMl = ebmMl,
            totalBottleIntakeMl = totalBottleMl,
            bottleSessionsCount = bottleFeeds.size,
            nursingSessionsCount = nursingFeeds.size,
            nursingTotalMinutes = nursingMins,
            averageBottleMl = avgBottle,
            averageIntervalMinutes = avgIntervalMins,
            wetDiapersCount = wetCount,
            dirtyDiapersCount = dirtyCount,
            sleepTotalMinutes = sleepMins
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailySummary())

    // Feeding Guideline Comparison
    val guidelineComparison: StateFlow<GuidelineComparison?> = combine(
        activeBaby, dailySummary, latestGrowth, useAap, _currentTicker
    ) { baby, summary, growth, aap, now ->
        if (baby == null) null
        else {
            val ageDays = WhoGrowthCalculator.calculateAgeDays(baby.birthTimestamp, now)
            val weightKg = growth?.weightKg ?: baby.birthWeightKg
            val weightDate = growth?.timestamp ?: baby.birthTimestamp
            FeedingGuidelineCalculator.compareIntake(
                recordedBottleMl = summary.totalBottleIntakeMl,
                directNursingMinutes = summary.nursingTotalMinutes,
                weightKg = weightKg,
                weightTimestamp = weightDate,
                useAap = aap,
                ageDays = ageDays
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Latest WHO Growth Assessment
    val latestWhoAssessment: StateFlow<GrowthAssessment?> = combine(
        activeBaby, latestGrowth, _currentTicker
    ) { baby, growth, now ->
        if (baby == null || growth?.weightKg == null) null
        else {
            val ageMonths = WhoGrowthCalculator.calculateAgeMonths(baby.birthTimestamp, growth.timestamp)
            WhoGrowthCalculator.assessWeightForAge(growth.weightKg, ageMonths, baby.sex)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Nursing Timer Control
    fun startNursingTimer(breast: NursingBreast) {
        val current = _nursingTimerState.value
        _nursingTimerState.value = current.copy(
            isRunning = true,
            activeBreast = breast,
            lastUsedBreast = breast
        )
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _nursingTimerState.value
                if (state.isRunning) {
                    if (state.activeBreast == NursingBreast.LEFT) {
                        _nursingTimerState.value = state.copy(leftSeconds = state.leftSeconds + 1)
                    } else if (state.activeBreast == NursingBreast.RIGHT) {
                        _nursingTimerState.value = state.copy(rightSeconds = state.rightSeconds + 1)
                    }
                }
            }
        }
    }

    fun pauseNursingTimer() {
        _nursingTimerState.value = _nursingTimerState.value.copy(isRunning = false)
        timerJob?.cancel()
    }

    fun finishAndSaveNursingSession(notes: String = "") {
        val state = _nursingTimerState.value
        val leftMins = (state.leftSeconds / 60).toInt()
        val rightMins = (state.rightSeconds / 60).toInt()
        val baby = activeBaby.value ?: return

        if (leftMins > 0 || rightMins > 0 || state.leftSeconds > 20 || state.rightSeconds > 20) {
            val totalMins = (state.leftSeconds + state.rightSeconds) / 60
            val feeding = FeedingEntry(
                babyId = baby.id,
                timestamp = System.currentTimeMillis() - ((state.leftSeconds + state.rightSeconds) * 1000),
                endTime = System.currentTimeMillis(),
                type = FeedingType.DIRECT_BREASTFEEDING,
                nursingLeftMinutes = if (leftMins == 0 && state.leftSeconds > 10) 1 else leftMins,
                nursingRightMinutes = if (rightMins == 0 && state.rightSeconds > 10) 1 else rightMins,
                lastBreastUsed = state.lastUsedBreast,
                notes = notes
            )
            viewModelScope.launch {
                repository.insertFeeding(feeding)
                scheduleReminderAfterFeed(baby, feeding.endTime ?: feeding.timestamp)
            }
        }

        pauseNursingTimer()
        _nursingTimerState.value = NursingTimerState(lastUsedBreast = state.lastUsedBreast)
    }

    // Actions: Add Feeding
    fun logBottleFeed(
        type: FeedingType,
        preparedMl: Double,
        remainingMl: Double,
        brand: String,
        caregiver: String,
        notes: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val baby = activeBaby.value ?: return
        val consumed = (preparedMl - remainingMl).coerceAtLeast(0.0)

        val feeding = FeedingEntry(
            babyId = baby.id,
            timestamp = timestamp,
            type = type,
            preparedMl = preparedMl,
            remainingMl = remainingMl,
            consumedMl = consumed,
            formulaBrand = brand,
            caregiver = caregiver,
            notes = notes
        )
        viewModelScope.launch {
            repository.insertFeeding(feeding)
            scheduleReminderAfterFeed(baby, timestamp)
        }
    }

    private fun scheduleReminderAfterFeed(baby: BabyProfile, feedTime: Long) {
        SmartReminderManager.onActivityLogged(getApplication(), baby, ReminderKind.FEED, feedTime)
    }

    fun snoozeReminder(minutes: Int) {
        val baby = activeBaby.value ?: return
        SmartReminderManager.snooze(getApplication(), baby, ReminderKind.FEED, minutes)
    }

    fun logDiaper(
        type: DiaperType,
        stoolColor: String,
        consistency: StoolConsistency?,
        amount: String,
        hasMucusOrBlood: Boolean,
        notes: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val baby = activeBaby.value ?: return
        val entry = DiaperEntry(
            babyId = baby.id,
            timestamp = timestamp,
            type = type,
            stoolColor = stoolColor,
            stoolConsistency = consistency,
            amount = amount,
            hasMucusOrBlood = hasMucusOrBlood,
            notes = notes
        )
        viewModelScope.launch {
            repository.insertDiaper(entry)
            SmartReminderManager.onActivityLogged(getApplication(), baby, ReminderKind.DIAPER, timestamp)
        }
    }

    fun logSleep(
        startTime: Long,
        endTime: Long,
        isNap: Boolean,
        quality: String,
        notes: String
    ) {
        val baby = activeBaby.value ?: return
        val entry = SleepEntry(
            babyId = baby.id,
            startTime = startTime,
            endTime = endTime,
            isNap = isNap,
            quality = quality,
            notes = notes
        )
        viewModelScope.launch {
            repository.insertSleep(entry)
            SmartReminderManager.onActivityLogged(getApplication(), baby, ReminderKind.SLEEP, endTime)
        }
    }

    fun logGrowth(
        weightKg: Double?,
        lengthCm: Double?,
        headCircumferenceCm: Double?,
        source: String,
        notes: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val baby = activeBaby.value ?: return
        val entry = GrowthEntry(
            babyId = baby.id,
            timestamp = timestamp,
            weightKg = weightKg,
            lengthCm = lengthCm,
            headCircumferenceCm = headCircumferenceCm,
            measurementSource = source,
            notes = notes
        )

        val ageDays = WhoGrowthCalculator.calculateAgeDays(baby.birthTimestamp, timestamp)
        val ageMonths = WhoGrowthCalculator.calculateAgeMonths(baby.birthTimestamp, timestamp)
        val weightAssess = if (weightKg != null) WhoGrowthCalculator.assessWeightForAge(weightKg, ageMonths, baby.sex) else null
        val lengthAssess = if (lengthCm != null) WhoGrowthCalculator.assessLengthForAge(lengthCm, ageMonths, baby.sex) else null
        val headAssess = if (headCircumferenceCm != null) WhoGrowthCalculator.assessHeadCircumferenceForAge(headCircumferenceCm, ageMonths, baby.sex) else null

        val childMeasurement = ChildMeasurement(
            childId = baby.id,
            timestamp = timestamp,
            ageInDays = ageDays,
            weightKg = weightKg,
            lengthCm = lengthCm,
            headCircumferenceCm = headCircumferenceCm,
            weightPercentile = weightAssess?.percentile,
            lengthPercentile = lengthAssess?.percentile,
            headCircumferencePercentile = headAssess?.percentile,
            weightZScore = weightAssess?.zScore,
            lengthZScore = lengthAssess?.zScore,
            headCircumferenceZScore = headAssess?.zScore,
            measurementSource = source,
            notes = notes
        )

        viewModelScope.launch {
            repository.insertGrowth(entry)
            repository.insertChildMeasurement(childMeasurement)
        }
    }

    fun logSymptom(
        symptomType: String,
        severity: String,
        formulaBrand: String,
        notes: String
    ) {
        val baby = activeBaby.value ?: return
        val entry = SymptomEntry(
            babyId = baby.id,
            timestamp = System.currentTimeMillis(),
            symptomType = symptomType,
            severity = severity,
            formulaBrand = formulaBrand,
            notes = notes
        )
        viewModelScope.launch { repository.insertSymptom(entry) }
    }

    fun logPumping(
        leftMl: Double,
        rightMl: Double,
        durationMinutes: Int,
        storage: String,
        notes: String
    ) {
        val baby = activeBaby.value ?: return
        val entry = PumpingEntry(
            babyId = baby.id,
            timestamp = System.currentTimeMillis(),
            leftOutputMl = leftMl,
            rightOutputMl = rightMl,
            totalMl = leftMl + rightMl,
            durationMinutes = durationMinutes,
            storageLocation = storage,
            notes = notes
        )
        viewModelScope.launch { repository.insertPumping(entry) }
    }

    fun addFormulaProduct(
        manufacturer: String,
        name: String,
        stage: String,
        tinGrams: Double,
        price: Double,
        waterRatio: Double,
        scoopsRatio: Double,
        scoopMass: Double
    ) {
        val product = FormulaProduct(
            manufacturer = manufacturer,
            productName = name,
            stage = stage,
            tinSizeGrams = tinGrams,
            price = price,
            waterQuantityPerUnitMl = waterRatio,
            scoopsPerUnit = scoopsRatio,
            scoopMassGrams = scoopMass,
            dateOpenedTimestamp = System.currentTimeMillis(),
            estimatedPowderRemainingGrams = tinGrams
        )
        viewModelScope.launch { repository.insertFormula(product) }
    }

    fun logBottleSafety(isRefrigerated: Boolean, notes: String) {
        val baby = activeBaby.value ?: return
        val entry = BottleSafetyEntry(
            babyId = baby.id,
            preparedTimestamp = System.currentTimeMillis(),
            isRefrigerated = isRefrigerated,
            notes = notes
        )
        viewModelScope.launch { repository.insertBottleSafety(entry) }
    }

    fun markBottleFeedingStarted(bottle: BottleSafetyEntry) {
        viewModelScope.launch {
            repository.updateBottleSafety(bottle.copy(feedingStartedTimestamp = System.currentTimeMillis()))
        }
    }

    fun discardBottle(bottle: BottleSafetyEntry) {
        viewModelScope.launch {
            repository.updateBottleSafety(bottle.copy(isDiscarded = true, discardedTimestamp = System.currentTimeMillis()))
        }
    }

    fun createBaby(
        name: String,
        birthTimestamp: Long,
        sex: SexCategory,
        birthWeightKg: Double?,
        birthLengthCm: Double?,
        birthHeadCircumferenceCm: Double?,
        gestationalWeeks: Int?,
        gestationalDays: Int?,
        feedingMethod: FeedingMethod,
        reminderInterval: Int
    ) {
        viewModelScope.launch {
            val baby = BabyProfile(
                name = name,
                birthTimestamp = birthTimestamp,
                sex = sex,
                birthWeightKg = birthWeightKg,
                birthLengthCm = birthLengthCm,
                birthHeadCircumferenceCm = birthHeadCircumferenceCm,
                gestationalAgeWeeks = gestationalWeeks,
                gestationalAgeDays = gestationalDays,
                primaryFeedingMethod = feedingMethod,
                reminderIntervalMinutes = reminderInterval,
                isDefault = allBabies.value.isEmpty()
            )
            val newId = repository.insertBaby(baby)
            preferences.setSelectedBabyId(newId)
        }
    }

    fun selectBaby(id: Long) {
        viewModelScope.launch {
            preferences.setSelectedBabyId(id)
            repository.setDefaultBaby(id)
        }
    }

    fun updateBabyProfile(baby: BabyProfile) {
        viewModelScope.launch {
            repository.updateBaby(baby)
            val existing = SmartReminderManager.getRule(getApplication(), baby.id, ReminderKind.FEED)
            if (existing.enabled && existing.intervalMinutes != baby.reminderIntervalMinutes && existing.fixedTimes.isBlank()) {
                SmartReminderManager.saveRule(getApplication(), baby, ReminderKind.FEED,
                    existing.copy(intervalMinutes = baby.reminderIntervalMinutes))
            }
        }
    }

    fun deleteItem(item: Any) {
        viewModelScope.launch {
            when (item) {
                is FeedingEntry -> repository.deleteFeeding(item)
                is GrowthEntry -> repository.deleteGrowth(item)
                is DiaperEntry -> repository.deleteDiaper(item)
                is SleepEntry -> repository.deleteSleep(item)
                is SymptomEntry -> repository.deleteSymptom(item)
            }
        }
    }
}
