package com.example.guidelines

import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

data class GuidelineComparison(
    val selectedGuidelineName: String, // NHS or AAP
    val referenceRangeLowerMl: Double,
    val referenceRangeUpperMl: Double,
    val actualRecordedBottleMl: Double,
    val directNursingMinutes: Int,
    val weightKgUsed: Double?,
    val weightMeasurementDate: Long?,
    val intakePerKgMl: Double?,
    val statusSummary: String,
    val sourceDescription: String,
    val isBreastfedOrMixed: Boolean
)

data class BottleSafetyStatus(
    val isExpired: Boolean,
    val remainingMinutes: Long,
    val ruleDescription: String,
    val discardDeadlineTimestamp: Long
)

object FeedingGuidelineCalculator {

    const val NHS_SOURCE_INFO = "UK National Health Service (NHS) – Infant Feeding Guidelines: ~150–200 mL formula per kg/day for full-term babies (1 week to ~6 months)."
    const val AAP_SOURCE_INFO = "American Academy of Pediatrics (AAP) – Pediatric Nutrition: ~2.5 fl oz (approx. 75 mL) formula per pound of body weight per day (~165 mL/kg/day), generally averaging up to ~32 fl oz (~960 mL) per day."
    const val CDC_BOTTLE_SOURCE = "US Centers for Disease Control and Prevention (CDC) – Infant Food Safety & Formula Preparation Guidelines."

    /**
     * Calculate NHS reference range: 150 to 200 mL per kg body weight per 24 hours
     */
    fun calculateNhsFormulaRange(weightKg: Double): Pair<Double, Double> {
        val lower = weightKg * 150.0
        val upper = weightKg * 200.0
        return Pair(lower, upper)
    }

    /**
     * Calculate AAP reference range: ~2.5 fl oz / lb = ~162.8 mL/kg.
     * Often referenced with a +/- 15% range around this baseline, capped gently around 960 mL.
     */
    fun calculateAapFormulaRange(weightKg: Double): Pair<Double, Double> {
        val weightLbs = weightKg * 2.20462
        val baseOz = weightLbs * 2.5
        val baseMl = baseOz * 29.5735
        val lower = (baseMl * 0.88).coerceAtMost(900.0)
        val upper = (baseMl * 1.12).coerceAtMost(960.0)
        return Pair(lower, upper)
    }

    /**
     * Compare actual 24-hour recorded intake against the selected guideline
     */
    fun compareIntake(
        recordedBottleMl: Double,
        directNursingMinutes: Int,
        weightKg: Double?,
        weightTimestamp: Long?,
        useAap: Boolean = false,
        ageDays: Long
    ): GuidelineComparison {
        val isBreastfeedingInvolved = directNursingMinutes > 0

        if (weightKg == null || weightKg <= 0.0) {
            return GuidelineComparison(
                selectedGuidelineName = if (useAap) "AAP" else "NHS",
                referenceRangeLowerMl = 0.0,
                referenceRangeUpperMl = 0.0,
                actualRecordedBottleMl = recordedBottleMl,
                directNursingMinutes = directNursingMinutes,
                weightKgUsed = null,
                weightMeasurementDate = null,
                intakePerKgMl = null,
                statusSummary = "Log baby's weight to see personalized WHO/NHS formula intake comparisons.",
                sourceDescription = if (useAap) AAP_SOURCE_INFO else NHS_SOURCE_INFO,
                isBreastfedOrMixed = isBreastfeedingInvolved
            )
        }

        val (lower, upper) = if (useAap) calculateAapFormulaRange(weightKg) else calculateNhsFormulaRange(weightKg)
        val intakePerKg = if (weightKg > 0) recordedBottleMl / weightKg else null

        val summary = when {
            isBreastfeedingInvolved -> {
                "Recorded: ${recordedBottleMl.roundToInt()} mL bottle intake plus $directNursingMinutes mins direct nursing. Note: nursing minutes cannot be converted to mL, so actual milk volume is higher than the bottle total alone."
            }
            ageDays < 7 -> {
                "Baby is in first week of life: newborns consume small volumes (30–60 mL per feed) as stomach expands. Follow responsive feeding and pediatrician guidance."
            }
            ageDays > 180 -> {
                "Baby is over 6 months: complementary foods are typically introduced alongside milk. Weight-only formula calculations may not reflect total nutritional intake."
            }
            recordedBottleMl in lower..upper -> {
                "Recorded formula intake (${recordedBottleMl.roundToInt()} mL) falls within the broad reference range (${lower.roundToInt()}–${upper.roundToInt()} mL/day)."
            }
            recordedBottleMl < lower -> {
                "Recorded intake (${recordedBottleMl.roundToInt()} mL) is currently below the general 24-hr reference (${lower.roundToInt()}–${upper.roundToInt()} mL). Feed responsively according to hunger cues."
            }
            else -> {
                "Recorded intake (${recordedBottleMl.roundToInt()} mL) is slightly above the typical 24-hr reference range (${lower.roundToInt()}–${upper.roundToInt()} mL). Individual needs vary."
            }
        }

        return GuidelineComparison(
            selectedGuidelineName = if (useAap) "AAP" else "NHS",
            referenceRangeLowerMl = lower,
            referenceRangeUpperMl = upper,
            actualRecordedBottleMl = recordedBottleMl,
            directNursingMinutes = directNursingMinutes,
            weightKgUsed = weightKg,
            weightMeasurementDate = weightTimestamp,
            intakePerKgMl = intakePerKg,
            statusSummary = summary,
            sourceDescription = if (useAap) AAP_SOURCE_INFO else NHS_SOURCE_INFO,
            isBreastfedOrMixed = isBreastfeedingInvolved
        )
    }

    /**
     * CDC Bottle Safety Rules:
     * 1. If feeding started: must discard within 1 hour (60 minutes).
     * 2. If unrefrigerated: must discard within 2 hours (120 minutes) of preparation.
     * 3. If refrigerated immediately: must discard within 24 hours.
     */
    fun calculateBottleSafety(
        preparedTimestamp: Long,
        feedingStartedTimestamp: Long?,
        isRefrigerated: Boolean,
        nowTimestamp: Long = System.currentTimeMillis()
    ): BottleSafetyStatus {
        val deadline: Long
        val rule: String

        if (feedingStartedTimestamp != null) {
            deadline = feedingStartedTimestamp + TimeUnit.HOURS.toMillis(1)
            rule = "CDC: Discard within 1 hour once feeding begins (bacteria from saliva multiply quickly)."
        } else if (isRefrigerated) {
            deadline = preparedTimestamp + TimeUnit.HOURS.toMillis(24)
            rule = "CDC: Prepared formula refrigerated immediately remains safe for up to 24 hours."
        } else {
            deadline = preparedTimestamp + TimeUnit.HOURS.toMillis(2)
            rule = "CDC: Prepared formula left at room temperature must be used within 2 hours."
        }

        val remainingMillis = deadline - nowTimestamp
        val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
        val isExpired = remainingMillis <= 0

        return BottleSafetyStatus(
            isExpired = isExpired,
            remainingMinutes = remainingMinutes.coerceAtLeast(0),
            ruleDescription = rule,
            discardDeadlineTimestamp = deadline
        )
    }

    /**
     * Per-feed Planning Calculator:
     * Daily target divided by planned feeds
     */
    fun calculatePerFeedAmount(dailyMl: Double, plannedFeedsCount: Int): Double {
        if (plannedFeedsCount <= 0) return 0.0
        return dailyMl / plannedFeedsCount.toDouble()
    }
}
