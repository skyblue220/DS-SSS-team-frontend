package com.sss.healthcare

import android.content.Context
import java.time.LocalDate
import java.time.LocalTime

object DemoDataSeeder {
    private const val PREF_NAME = "demo_data_seed"
    private const val KEY_SEEDED = "recent_two_weeks_seeded_v1"

    suspend fun seedRecentTwoWeeksIfNeeded(
        context: Context,
        sleepDataManager: SleepDataManager,
        exerciseDataManager: ExerciseDataManager,
        diseaseDataManager: DiseaseDataManager,
        medicationListManager: MedicationListManager
    ) {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (preferences.getBoolean(KEY_SEEDED, false)) return

        val selectedDisease = ProfileSettingsStore.load(context).selectedDisease
        val today = LocalDate.now()
        val medicationNames = listOf("아침약", "혈압약", "영양제")

        for (offset in 13 downTo 0) {
            val date = today.minusDays(offset.toLong())
            val index = 13 - offset

            sleepDataManager.saveSleepData(
                date,
                SleepRecord(
                    bedtime = LocalTime.of(22 + (index % 2), if (index % 3 == 0) 40 else 20),
                    wakeupTime = LocalTime.of(6 + (index % 3), if (index % 2 == 0) 30 else 50)
                )
            )

            exerciseDataManager.saveExerciseData(
                date,
                ExerciseRecord(
                    steps = (18 + (index % 5) * 4).toString(),
                    runTime = (12 + (index % 6) * 5).toString(),
                    runDistance = String.format(java.util.Locale.US, "%.1f", 1.2 + (index % 5) * 0.45),
                    cycleTime = (if (index % 4 == 0) 20 else 0).toString(),
                    cycleDistance = String.format(java.util.Locale.US, "%.1f", if (index % 4 == 0) 4.5 + index else 0.0)
                )
            )

            diseaseDataManager.saveDiseaseData(
                date,
                demoDiseaseRecord(selectedDisease, index)
            )

            medicationListManager.saveMedicationList(
                date,
                medicationNames.mapIndexed { medIndex, name ->
                    MedicationItem(
                        name = name,
                        time = listOf("08:00", "13:00", "21:00")[medIndex],
                        period = "매일",
                        useNotification = true,
                        notificationTime = "정각에 알림",
                        isTaken = when {
                            date == today -> medIndex == 0
                            index % 6 == 0 -> medIndex < 2
                            else -> true
                        }
                    )
                }
            )
        }

        preferences.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private fun demoDiseaseRecord(selectedDisease: String, index: Int): DiseaseRecord {
        val condition = listOf(7f, 8f, 6f, 7f, 8f, 7f, 9f)[index % 7]

        return when (selectedDisease) {
            "당뇨" -> DiseaseRecord(
                conditionScore = condition,
                selectedDisease = selectedDisease,
                bloodSugar = listOf("104", "126", "145", "168", "118", "132", "152")[index % 7],
                glucoseTiming = listOf("식전", "식후", "취침 전")[index % 3],
                symptoms = when (index % 5) {
                    0 -> setOf("피로감")
                    1 -> setOf("심한 갈증", "소변 증가")
                    else -> emptySet()
                },
                memo = "시연용 당뇨 기록"
            )

            "류마티스 관절염" -> DiseaseRecord(
                conditionScore = condition,
                selectedDisease = selectedDisease,
                jointPainScore = listOf(2f, 4f, 3f, 5f, 2f, 6f, 3f)[index % 7],
                painAreas = when (index % 4) {
                    0 -> setOf("손", "손목")
                    1 -> setOf("무릎")
                    2 -> setOf("어깨")
                    else -> setOf("발")
                },
                morningStiffness = listOf("없음", "30분 이하", "30분~1시간", "없음")[index % 4],
                jointSwelling = if (index % 5 == 0) "있음" else "없음",
                fatigueScore = listOf(2f, 3f, 4f, 5f, 3f, 6f, 2f)[index % 7],
                memo = "시연용 류마티스 관절염 기록"
            )

            else -> DiseaseRecord(
                conditionScore = condition,
                selectedDisease = "고혈압",
                systolicBloodPressure = listOf("118", "124", "132", "136", "121", "142", "128")[index % 7],
                diastolicBloodPressure = listOf("76", "78", "84", "86", "79", "92", "80")[index % 7],
                pulse = listOf("68", "72", "75", "70", "74", "78", "71")[index % 7],
                symptoms = when (index % 6) {
                    0 -> setOf("두통")
                    1 -> setOf("어지러움")
                    2 -> setOf("가슴 답답함", "숨참")
                    else -> emptySet()
                },
                memo = "시연용 고혈압 기록"
            )
        }
    }
}
