package com.example.export

import android.content.Context
import android.content.Intent
import com.example.data.model.*
import com.example.who.WhoGrowthCalculator
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object DataExportManager {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dayOnlyFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun generateFeedingCsv(feedings: List<FeedingEntry>): String {
        val sb = StringBuilder()
        sb.append("ID,Date_Time,Type,Prepared_mL,Remaining_mL,Consumed_mL,Formula_Brand,Nursing_Left_Min,Nursing_Right_Min,Caregiver,Notes\n")
        feedings.forEach { f ->
            sb.append("${f.id},")
            sb.append("\"${dateFormatter.format(Date(f.timestamp))}\",")
            sb.append("${f.type.name},")
            sb.append("${f.preparedMl},")
            sb.append("${f.remainingMl},")
            sb.append("${f.consumedMl},")
            sb.append("\"${f.formulaBrand}\",")
            sb.append("${f.nursingLeftMinutes},")
            sb.append("${f.nursingRightMinutes},")
            sb.append("\"${f.caregiver}\",")
            sb.append("\"${f.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    fun generateGrowthCsv(growths: List<GrowthEntry>, baby: BabyProfile): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Age_Days,Weight_kg,Length_cm,Head_Circumference_cm,Weight_WHO_Percentile,Source,Notes\n")
        growths.forEach { g ->
            val ageDays = WhoGrowthCalculator.calculateAgeDays(baby.birthTimestamp, g.timestamp)
            val ageMonths = WhoGrowthCalculator.calculateAgeMonths(baby.birthTimestamp, g.timestamp)
            val weightPercentile = if (g.weightKg != null) {
                WhoGrowthCalculator.assessWeightForAge(g.weightKg, ageMonths, baby.sex).percentile.roundToInt()
            } else {
                ""
            }
            sb.append("${g.id},")
            sb.append("\"${dayOnlyFormatter.format(Date(g.timestamp))}\",")
            sb.append("$ageDays,")
            sb.append("${g.weightKg ?: ""},")
            sb.append("${g.lengthCm ?: ""},")
            sb.append("${g.headCircumferenceCm ?: ""},")
            sb.append("$weightPercentile,")
            sb.append("\"${g.measurementSource}\",")
            sb.append("\"${g.notes.replace("\"", "\"\"")}\"\n")
        }
        return sb.toString()
    }

    /**
     * Generates a comprehensive Pediatrician Clinical Summary Report
     */
    fun generatePediatricianReport(
        baby: BabyProfile,
        feedings: List<FeedingEntry>,
        growths: List<GrowthEntry>,
        diapers: List<DiaperEntry>,
        symptoms: List<SymptomEntry>
    ): String {
        val now = System.currentTimeMillis()
        val totalAgeDays = WhoGrowthCalculator.calculateAgeDays(baby.birthTimestamp, now)
        val months = totalAgeDays / 30
        val remainingWeeks = (totalAgeDays % 30) / 7
        val remainingDays = (totalAgeDays % 30) % 7

        val (correctedDays, isPremature) = WhoGrowthCalculator.calculateCorrectedAgeDays(
            baby.birthTimestamp,
            now,
            baby.gestationalAgeWeeks,
            baby.gestationalAgeDays
        )

        // Rolling 7-day or total stats
        val totalFormulaConsumed = feedings.filter { it.type == FeedingType.FORMULA || it.type == FeedingType.MIXED_BOTTLE }
            .sumOf { it.consumedMl }
        val totalEbmConsumed = feedings.filter { it.type == FeedingType.EXPRESSED_BREAST_MILK }
            .sumOf { it.consumedMl }
        val totalBottleIntake = totalFormulaConsumed + totalEbmConsumed
        val directNursingSessions = feedings.filter { it.type == FeedingType.DIRECT_BREASTFEEDING }
        val totalNursingMinutes = directNursingSessions.sumOf { it.nursingLeftMinutes + it.nursingRightMinutes }

        val sb = StringBuilder()
        sb.append("==================================================\n")
        sb.append("        LITTLEROUTINE PEDIATRICIAN REPORT        \n")
        sb.append("==================================================\n\n")

        sb.append("1. PATIENT DEMOGRAPHICS & PROFILE\n")
        sb.append("--------------------------------------------------\n")
        sb.append("Baby Name: ${baby.name}\n")
        sb.append("Date of Birth: ${dayOnlyFormatter.format(Date(baby.birthTimestamp))}\n")
        sb.append("Sex Reference: ${baby.sex.name}\n")
        sb.append("Chronological Age: $months months, $remainingWeeks weeks, $remainingDays days ($totalAgeDays days total)\n")
        if (isPremature) {
            sb.append("Gestational Age at Birth: ${baby.gestationalAgeWeeks}w ${baby.gestationalAgeDays ?: 0}d (Premature)\n")
            sb.append("Corrected Age: ${correctedDays / 30} months, ${(correctedDays % 30) / 7} weeks ($correctedDays days)\n")
        }
        sb.append("Primary Feeding Routine: ${baby.primaryFeedingMethod.name.replace("_", " ")}\n")
        baby.birthWeightKg?.let { sb.append("Birth Weight: $it kg\n") }
        baby.birthLengthCm?.let { sb.append("Birth Length: $it cm\n") }
        baby.birthHeadCircumferenceCm?.let { sb.append("Birth Head Circumference: $it cm\n") }
        sb.append("\n")

        sb.append("2. RECORDED NUTRITION & FEEDING SUMMARY\n")
        sb.append("--------------------------------------------------\n")
        sb.append("Total Recorded Bottle Sessions: ${feedings.count { it.type != FeedingType.DIRECT_BREASTFEEDING }}\n")
        sb.append("Formula Consumed: ${totalFormulaConsumed.roundToInt()} mL\n")
        sb.append("Expressed Breast Milk (EBM) Consumed: ${totalEbmConsumed.roundToInt()} mL\n")
        sb.append("Total Measurable Bottle Intake: ${totalBottleIntake.roundToInt()} mL\n")
        sb.append("Direct Nursing Sessions: ${directNursingSessions.size} sessions ($totalNursingMinutes total minutes)\n")
        sb.append("* CLINICAL NOTE: Total recorded bottle volume does NOT include unmeasured milk from direct breastfeeding sessions.\n\n")

        sb.append("3. GROWTH HISTORY & WHO REFERENCE PERCENTILES\n")
        sb.append("--------------------------------------------------\n")
        sb.append("WHO Child Growth Standards (2006 MGRS) Reference:\n")
        if (growths.isEmpty()) {
            sb.append("No growth measurements logged yet.\n")
        } else {
            growths.forEach { g ->
                val gAgeDays = WhoGrowthCalculator.calculateAgeDays(baby.birthTimestamp, g.timestamp)
                val gAgeMonths = WhoGrowthCalculator.calculateAgeMonths(baby.birthTimestamp, g.timestamp)
                sb.append("• ${dayOnlyFormatter.format(Date(g.timestamp))} (Age: ${gAgeDays}d / ${"%.1f".format(gAgeMonths)}m):\n")
                g.weightKg?.let { w ->
                    val assess = WhoGrowthCalculator.assessWeightForAge(w, gAgeMonths, baby.sex)
                    sb.append("   - Weight: $w kg (WHO ~${assess.percentile.roundToInt()}th percentile, Z-Score: ${"%.2f".format(assess.zScore)})\n")
                }
                g.lengthCm?.let { l ->
                    val assess = WhoGrowthCalculator.assessLengthForAge(l, gAgeMonths, baby.sex)
                    sb.append("   - Length: $l cm (WHO ~${assess.percentile.roundToInt()}th percentile)\n")
                }
                g.headCircumferenceCm?.let { hc ->
                    val assess = WhoGrowthCalculator.assessHeadCircumferenceForAge(hc, gAgeMonths, baby.sex)
                    sb.append("   - Head Circumference: $hc cm (WHO ~${assess.percentile.roundToInt()}th percentile)\n")
                }
            }
        }
        sb.append("\n")

        sb.append("4. DIAPER & STOOL PATTERNS\n")
        sb.append("--------------------------------------------------\n")
        val wetCount = diapers.count { it.type == DiaperType.WET || it.type == DiaperType.BOTH }
        val dirtyCount = diapers.count { it.type == DiaperType.DIRTY || it.type == DiaperType.BOTH }
        sb.append("Total Logged Diapers: ${diapers.size} (Wet: $wetCount, Dirty: $dirtyCount)\n")
        val unusualDiapers = diapers.filter { it.hasMucusOrBlood || it.stoolConsistency == StoolConsistency.PELLET_LIKE || it.stoolConsistency == StoolConsistency.HARD }
        if (unusualDiapers.isNotEmpty()) {
            sb.append("Observations of note:\n")
            unusualDiapers.take(5).forEach { d ->
                sb.append(" • ${dayOnlyFormatter.format(Date(d.timestamp))}: ${d.stoolConsistency?.name ?: "Consistency noted"}, Mucus/Blood flag: ${d.hasMucusOrBlood}\n")
            }
        }
        sb.append("\n")

        sb.append("5. SYMPTOMS & REACTION LOGS\n")
        sb.append("--------------------------------------------------\n")
        if (symptoms.isEmpty()) {
            sb.append("No specific adverse symptoms or feeding refusal recorded.\n")
        } else {
            symptoms.take(10).forEach { s ->
                sb.append(" • ${dayOnlyFormatter.format(Date(s.timestamp))}: ${s.symptomType} (${s.severity}) ${if (s.notes.isNotBlank()) "- ${s.notes}" else ""}\n")
            }
        }

        sb.append("\n==================================================\n")
        sb.append("Report generated on ${dateFormatter.format(Date(now))} via LittleRoutine.\n")
        sb.append("Standards Reference: World Health Organization (WHO) & UK NHS guidelines.\n")
        sb.append("==================================================\n")

        return sb.toString()
    }

    fun shareText(context: Context, text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }
}
