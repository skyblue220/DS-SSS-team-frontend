package com.sss.healthcare

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

enum class DiseaseType {
    HYPERTENSION,
    DIABETES,
    RHEUMATOID_ARTHRITIS
}

enum class MedicationStatus {
    COMPLETED,
    SCHEDULED,
    MISSED
}

fun diseaseTypeFromName(name: String): DiseaseType {
    return when (name) {
        "당뇨" -> DiseaseType.DIABETES
        "류마티스 관절염" -> DiseaseType.RHEUMATOID_ARTHRITIS
        else -> DiseaseType.HYPERTENSION
    }
}

fun calculateHealthScore(
    diseaseType: DiseaseType,
    record: DiseaseRecord?,
    medicationStatus: MedicationStatus
): Int {
    val safeRecord = record ?: DiseaseRecord()
    val rawScore = when (diseaseType) {
        DiseaseType.HYPERTENSION -> calculateHypertensionScore(safeRecord, medicationStatus)
        DiseaseType.DIABETES -> calculateDiabetesScore(safeRecord, medicationStatus)
        DiseaseType.RHEUMATOID_ARTHRITIS -> calculateRheumatoidScore(safeRecord, medicationStatus)
    }

    return rawScore.roundToInt().coerceIn(0, 100)
}

fun calculateMedicationStatus(
    items: List<MedicationItem>,
    date: LocalDate,
    nowDate: LocalDate = LocalDate.now(),
    nowTime: LocalTime = LocalTime.now()
): MedicationStatus {
    if (items.isEmpty()) return MedicationStatus.SCHEDULED
    if (items.all { it.isTaken }) return MedicationStatus.COMPLETED

    val hasMissed = items.any { item ->
        !item.isTaken && (date.isBefore(nowDate) || (date == nowDate && parseMedicationTime(item.time).isBefore(nowTime)))
    }

    return if (hasMissed) MedicationStatus.MISSED else MedicationStatus.SCHEDULED
}

private fun calculateHypertensionScore(
    record: DiseaseRecord,
    medicationStatus: MedicationStatus
): Float {
    val conditionPart = record.conditionScore * 4f
    val bloodPressurePart = calculateBloodPressurePart(
        systolic = record.systolicBloodPressure.toIntOrNull(),
        diastolic = record.diastolicBloodPressure.toIntOrNull()
    )
    val symptomPart = symptomPart(record.symptoms.size)
    val medicationPart = medicationPart(medicationStatus)

    return conditionPart + bloodPressurePart + symptomPart + medicationPart
}

private fun calculateDiabetesScore(
    record: DiseaseRecord,
    medicationStatus: MedicationStatus
): Float {
    val conditionPart = record.conditionScore * 3.5f
    val bloodGlucosePart = calculateBloodGlucosePart(
        glucose = record.bloodSugar.toIntOrNull(),
        timing = record.glucoseTiming
    )
    val symptomPart = symptomPart(record.symptoms.size)
    val medicationPart = medicationPart(medicationStatus)

    return conditionPart + bloodGlucosePart + symptomPart + medicationPart
}

private fun calculateRheumatoidScore(
    record: DiseaseRecord,
    medicationStatus: MedicationStatus
): Float {
    val conditionPart = record.conditionScore * 3f
    val painPart = 25f - (record.jointPainScore * 2.5f)
    val stiffnessPart = when (record.morningStiffness) {
        "없음" -> 15f
        "30분 이하" -> 12f
        "30분~1시간" -> 8f
        "1시간 이상" -> 4f
        else -> 8f
    }
    val swellingPart = when (record.jointSwelling) {
        "없음" -> 10f
        "있음" -> 4f
        else -> 7f
    }
    val fatiguePart = 10f - record.fatigueScore
    val medicationPart = medicationPart(medicationStatus)

    return conditionPart + painPart + stiffnessPart + swellingPart + fatiguePart + medicationPart
}

private fun calculateBloodPressurePart(systolic: Int?, diastolic: Int?): Float {
    if (systolic == null || diastolic == null) return 15f

    return when {
        systolic >= 180 || diastolic >= 120 -> 5f
        systolic in 140..179 || diastolic in 90..119 -> 14f
        systolic in 130..139 || diastolic in 80..89 -> 22f
        systolic in 120..129 && diastolic < 80 -> 26f
        systolic < 120 && diastolic < 80 -> 30f
        else -> 15f
    }
}

private fun calculateBloodGlucosePart(glucose: Int?, timing: String): Float {
    if (glucose == null) return 18f

    return if (timing == "식후") {
        when {
            glucose < 70 -> 5f
            glucose <= 180 -> 35f
            glucose <= 250 -> 22f
            else -> 12f
        }
    } else {
        when {
            glucose < 70 -> 5f
            glucose <= 130 -> 35f
            glucose <= 180 -> 27f
            glucose <= 250 -> 18f
            else -> 10f
        }
    }
}

private fun symptomPart(symptomCount: Int): Float {
    return when (symptomCount) {
        0 -> 20f
        1 -> 14f
        2 -> 9f
        else -> 5f
    }
}

private fun medicationPart(status: MedicationStatus): Float {
    return when (status) {
        MedicationStatus.COMPLETED -> 10f
        MedicationStatus.SCHEDULED -> 7f
        MedicationStatus.MISSED -> 3f
    }
}

private fun parseMedicationTime(time: String): LocalTime {
    return try {
        val parts = time.split(":")
        LocalTime.of(parts[0].trim().toInt(), parts[1].trim().toInt())
    } catch (e: Exception) {
        LocalTime.of(23, 59)
    }
}
