package com.example.who

import com.example.data.model.SexCategory
import kotlin.math.*

data class GrowthAssessment(
    val zScore: Double,
    val percentile: Double,
    val median50th: Double,
    val referenceP3: Double,
    val referenceP15: Double,
    val referenceP85: Double,
    val referenceP97: Double,
    val description: String
)

data class GrowthVelocity(
    val weightGainGrams: Double,
    val daysBetween: Long,
    val gramsPerDay: Double,
    val gramsPerWeek: Double,
    val lengthGainCm: Double? = null,
    val headCircumferenceGainCm: Double? = null
)

object WhoGrowthCalculator {

    /**
     * Exact WHO LMS Formula:
     * When L != 0: Z = ((X / M)^L - 1) / (L * S)
     * When L == 0: Z = ln(X / M) / S
     */
    fun calculateZScore(measurement: Double, lms: LmsPoint): Double {
        if (measurement <= 0.0 || lms.m <= 0.0 || lms.s <= 0.0) return 0.0
        val l = lms.l
        val m = lms.m
        val s = lms.s

        return if (abs(l) > 0.0001) {
            val ratio = measurement / m
            (ratio.pow(l) - 1.0) / (l * s)
        } else {
            ln(measurement / m) / s
        }
    }

    /**
     * Standard Normal Cumulative Distribution Function (CDF)
     * Converts Z-score to Percentile (0.0 to 100.0)
     */
    fun zScoreToPercentile(z: Double): Double {
        if (z < -5.0) return 0.01
        if (z > 5.0) return 99.99

        // Approximation of standard normal CDF via error function
        val cdf = 0.5 * (1.0 + erf(z / sqrt(2.0)))
        return (cdf * 100.0).coerceIn(0.01, 99.99)
    }

    /**
     * Inverse calculation: Measurement value X for a given Z-score and LMS point
     * X = M * (1 + L * S * Z)^(1 / L)
     */
    fun zScoreToValue(z: Double, lms: LmsPoint): Double {
        val l = lms.l
        val m = lms.m
        val s = lms.s

        return if (abs(l) > 0.0001) {
            val inner = 1.0 + l * s * z
            if (inner > 0) m * inner.pow(1.0 / l) else m
        } else {
            m * exp(s * z)
        }
    }

    /**
     * Percentile to measurement value
     * Percentile 50 -> Z = 0
     * Percentile 3 -> Z = -1.8808
     * Percentile 15 -> Z = -1.0364
     * Percentile 85 -> Z = +1.0364
     * Percentile 97 -> Z = +1.8808
     */
    fun percentileToValue(percentile: Double, lms: LmsPoint): Double {
        val z = when {
            percentile <= 3.0 -> -1.8808
            percentile <= 15.0 -> -1.0364
            percentile in 49.0..51.0 -> 0.0
            percentile >= 97.0 -> 1.8808
            percentile >= 85.0 -> 1.0364
            else -> {
                // Approximate inverse normal CDF
                val p = percentile / 100.0
                approximateInverseNormal(p)
            }
        }
        return zScoreToValue(z, lms)
    }

    /**
     * Abramowitz and Stegun error function approximation
     */
    private fun erf(x: Double): Double {
        val a1 = 0.254829592
        val a2 = -0.284496736
        val a3 = 1.421413741
        val a4 = -1.453152027
        val a5 = 1.061405429
        val p = 0.3275911

        val sign = if (x < 0) -1.0 else 1.0
        val absX = abs(x)
        val t = 1.0 / (1.0 + p * absX)
        val y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * exp(-absX * absX)
        return sign * y
    }

    private fun approximateInverseNormal(p: Double): Double {
        // Rational approximation for normal quantile
        val clampedP = p.coerceIn(0.0001, 0.9999)
        val t = sqrt(-2.0 * ln(if (clampedP < 0.5) clampedP else 1.0 - clampedP))
        val c0 = 2.515517
        val c1 = 0.802853
        val c2 = 0.010328
        val d1 = 1.432788
        val d2 = 0.189269
        val d3 = 0.001308
        val z = t - (c0 + c1 * t + c2 * t * t) / (1.0 + d1 * t + d2 * t * t + d3 * t * t * t)
        return if (clampedP < 0.5) -z else z
    }

    /**
     * Linear interpolation between LMS points
     */
    fun interpolateLms(targetIndex: Double, table: List<LmsPoint>): LmsPoint {
        if (table.isEmpty()) return LmsPoint(0.0, 1.0, 1.0, 0.1)
        if (targetIndex <= table.first().indexValue) return table.first()
        if (targetIndex >= table.last().indexValue) return table.last()

        for (i in 0 until table.size - 1) {
            val p1 = table[i]
            val p2 = table[i + 1]
            if (targetIndex >= p1.indexValue && targetIndex <= p2.indexValue) {
                val span = p2.indexValue - p1.indexValue
                if (span <= 0.0) return p1
                val frac = (targetIndex - p1.indexValue) / span
                return LmsPoint(
                    indexValue = targetIndex,
                    l = p1.l + frac * (p2.l - p1.l),
                    m = p1.m + frac * (p2.m - p1.m),
                    s = p1.s + frac * (p2.s - p1.s)
                )
            }
        }
        return table.last()
    }

    /**
     * Calculate Age in exact days and fractional months
     */
    fun calculateAgeDays(birthTimestamp: Long, measurementTimestamp: Long): Long {
        val diff = measurementTimestamp - birthTimestamp
        return (diff / (1000L * 60 * 60 * 24)).coerceAtLeast(0)
    }

    fun calculateAgeMonths(birthTimestamp: Long, measurementTimestamp: Long): Double {
        val days = calculateAgeDays(birthTimestamp, measurementTimestamp)
        return days / 30.4375 // Standard WHO average days per month
    }

    /**
     * Corrected Age calculation for premature infants (<37 weeks)
     * Full term is considered 40 weeks.
     */
    fun calculateCorrectedAgeDays(
        birthTimestamp: Long,
        measurementTimestamp: Long,
        gestationalAgeWeeks: Int?,
        gestationalAgeDays: Int?
    ): Pair<Long, Boolean> {
        val chronologicalDays = calculateAgeDays(birthTimestamp, measurementTimestamp)
        if (gestationalAgeWeeks == null || gestationalAgeWeeks >= 37) {
            return Pair(chronologicalDays, false)
        }
        val daysPremature = (40 - gestationalAgeWeeks) * 7 - (gestationalAgeDays ?: 0)
        val correctedDays = (chronologicalDays - daysPremature).coerceAtLeast(0)
        return Pair(correctedDays, true)
    }

    /**
     * Comprehensive WHO Assessment for Weight-for-Age
     */
    fun assessWeightForAge(
        weightKg: Double,
        ageMonths: Double,
        sex: SexCategory
    ): GrowthAssessment {
        val table = if (sex == SexCategory.BOY) WhoLmsData.weightForAgeBoys else WhoLmsData.weightForAgeGirls
        val lms = interpolateLms(ageMonths, table)
        val z = calculateZScore(weightKg, lms)
        val p = zScoreToPercentile(z)

        val desc = when {
            p < 3.0 -> "Below 3rd percentile (< -1.88 SD). Review with pediatrician."
            p in 3.0..15.0 -> "3rd - 15th percentile. Tracking on lower curve."
            p in 15.0..85.0 -> "Within expected central range (15th - 85th percentile)."
            p in 85.0..97.0 -> "85th - 97th percentile. Tracking on upper curve."
            else -> "Above 97th percentile (> +1.88 SD). Review with pediatrician."
        }

        return GrowthAssessment(
            zScore = z,
            percentile = p,
            median50th = lms.m,
            referenceP3 = percentileToValue(3.0, lms),
            referenceP15 = percentileToValue(15.0, lms),
            referenceP85 = percentileToValue(85.0, lms),
            referenceP97 = percentileToValue(97.0, lms),
            description = desc
        )
    }

    /**
     * Comprehensive WHO Assessment for Length-for-Age
     */
    fun assessLengthForAge(
        lengthCm: Double,
        ageMonths: Double,
        sex: SexCategory
    ): GrowthAssessment {
        val table = if (sex == SexCategory.BOY) WhoLmsData.lengthForAgeBoys else WhoLmsData.lengthForAgeGirls
        val lms = interpolateLms(ageMonths, table)
        val z = calculateZScore(lengthCm, lms)
        val p = zScoreToPercentile(z)

        return GrowthAssessment(
            zScore = z,
            percentile = p,
            median50th = lms.m,
            referenceP3 = percentileToValue(3.0, lms),
            referenceP15 = percentileToValue(15.0, lms),
            referenceP85 = percentileToValue(85.0, lms),
            referenceP97 = percentileToValue(97.0, lms),
            description = "Length is approximately at the ${p.roundToInt()}th percentile."
        )
    }

    /**
     * Comprehensive WHO Assessment for Head Circumference-for-Age
     */
    fun assessHeadCircumferenceForAge(
        headCm: Double,
        ageMonths: Double,
        sex: SexCategory
    ): GrowthAssessment {
        val table = if (sex == SexCategory.BOY) WhoLmsData.headCircumferenceBoys else WhoLmsData.headCircumferenceGirls
        val lms = interpolateLms(ageMonths, table)
        val z = calculateZScore(headCm, lms)
        val p = zScoreToPercentile(z)

        return GrowthAssessment(
            zScore = z,
            percentile = p,
            median50th = lms.m,
            referenceP3 = percentileToValue(3.0, lms),
            referenceP15 = percentileToValue(15.0, lms),
            referenceP85 = percentileToValue(85.0, lms),
            referenceP97 = percentileToValue(97.0, lms),
            description = "Head circumference is at the ${p.roundToInt()}th percentile."
        )
    }

    /**
     * Growth Velocity Calculation between two measurements
     */
    fun calculateVelocity(
        prevTimestamp: Long,
        prevWeightKg: Double?,
        prevLengthCm: Double?,
        prevHeadCm: Double?,
        currentTimestamp: Long,
        currentWeightKg: Double?,
        currentLengthCm: Double?,
        currentHeadCm: Double?
    ): GrowthVelocity? {
        val days = ((currentTimestamp - prevTimestamp) / (1000L * 60 * 60 * 24)).coerceAtLeast(1)
        if (prevWeightKg == null || currentWeightKg == null) return null

        val weightGainGrams = (currentWeightKg - prevWeightKg) * 1000.0
        val gramsPerDay = weightGainGrams / days.toDouble()
        val gramsPerWeek = gramsPerDay * 7.0

        val lengthGain = if (prevLengthCm != null && currentLengthCm != null) currentLengthCm - prevLengthCm else null
        val headGain = if (prevHeadCm != null && currentHeadCm != null) currentHeadCm - prevHeadCm else null

        return GrowthVelocity(
            weightGainGrams = weightGainGrams,
            daysBetween = days,
            gramsPerDay = gramsPerDay,
            gramsPerWeek = gramsPerWeek,
            lengthGainCm = lengthGain,
            headCircumferenceGainCm = headGain
        )
    }
}
