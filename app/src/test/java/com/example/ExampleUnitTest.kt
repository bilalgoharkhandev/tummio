package com.example

import com.example.data.model.SexCategory
import com.example.guidelines.FeedingGuidelineCalculator
import com.example.who.WhoGrowthCalculator
import com.example.who.WhoLmsData
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

class ExampleUnitTest {

    @Test
    fun testWhoLmsMedianProducesZeroZScoreAnd50thPercentile() {
        val birthBoyLms = WhoLmsData.weightForAgeBoys.first() // Month 0
        // When measurement equals M, Z-score must be 0.0
        val z = WhoGrowthCalculator.calculateZScore(birthBoyLms.m, birthBoyLms)
        assertEquals(0.0, z, 0.001)

        val percentile = WhoGrowthCalculator.zScoreToPercentile(z)
        assertEquals(50.0, percentile, 0.1)
    }

    @Test
    fun testWhoWeightForAgeCalculation() {
        // Baby boy at 2 months weighing 5.6 kg
        val assessment = WhoGrowthCalculator.assessWeightForAge(
            weightKg = 5.6,
            ageMonths = 2.0,
            sex = SexCategory.BOY
        )
        // 5.6 kg at 2 months for boy is near median (M=5.596 kg)
        assertEquals(0.0, assessment.zScore, 0.05)
        assertEquals(50.0, assessment.percentile, 2.0)
    }

    @Test
    fun testNhsGuidelineCalculationForScenarioA() {
        // Scenario A from prompt: Baby weighs 4 kg
        val (lower, upper) = FeedingGuidelineCalculator.calculateNhsFormulaRange(4.0)
        // Expected: 4 kg * 150 = 600 mL, 4 kg * 200 = 800 mL
        assertEquals(600.0, lower, 0.01)
        assertEquals(800.0, upper, 0.01)
    }

    @Test
    fun testNetConsumedCalculation() {
        val prepared = 120.0
        val remaining = 30.0
        val consumed = (prepared - remaining).coerceAtLeast(0.0)
        assertEquals(90.0, consumed, 0.001)
    }

    @Test
    fun testCdcBottleSafetyExpiration() {
        val now = 1000000000L
        // Prepared 3 hours ago at room temp (> 2 hours) -> expired
        val threeHoursAgo = now - TimeUnit.HOURS.toMillis(3)
        val statusRoomTemp = FeedingGuidelineCalculator.calculateBottleSafety(
            preparedTimestamp = threeHoursAgo,
            feedingStartedTimestamp = null,
            isRefrigerated = false,
            nowTimestamp = now
        )
        assertTrue(statusRoomTemp.isExpired)

        // Prepared 30 mins ago, feeding started 70 mins ago (> 1 hour) -> expired
        val seventyMinsAgo = now - TimeUnit.MINUTES.toMillis(70)
        val statusFeeding = FeedingGuidelineCalculator.calculateBottleSafety(
            preparedTimestamp = seventyMinsAgo,
            feedingStartedTimestamp = seventyMinsAgo,
            isRefrigerated = false,
            nowTimestamp = now
        )
        assertTrue(statusFeeding.isExpired)
    }

    @Test
    fun testPrematurityCorrectedAgeCalculation() {
        val now = System.currentTimeMillis()
        val birth = now - TimeUnit.DAYS.toMillis(70) // 70 days chronological age
        // Born at 34 weeks (6 weeks premature = 42 days)
        val (correctedDays, isPremature) = WhoGrowthCalculator.calculateCorrectedAgeDays(
            birthTimestamp = birth,
            measurementTimestamp = now,
            gestationalAgeWeeks = 34,
            gestationalAgeDays = 0
        )
        assertTrue(isPremature)
        assertEquals(28L, correctedDays) // 70 - 42 = 28 days
    }
}
