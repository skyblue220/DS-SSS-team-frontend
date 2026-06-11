package com.sss.healthcare

import java.time.LocalDate

data class DailyHealthRecord(
    val date: LocalDate,
    val sleepHours: Float,
    val exerciseMinutes: Int
)

fun healthRecordFor(date: LocalDate): DailyHealthRecord {
    return DailyHealthRecord(
        date = date,
        sleepHours = if (date.dayOfMonth % 2 == 0) 8f else 5f,
        exerciseMinutes = date.dayOfMonth * 2
    )
}

fun recentSevenHealthRecords(referenceDate: LocalDate = LocalDate.now()): List<DailyHealthRecord> {
    return (6 downTo 0).map { daysAgo ->
        healthRecordFor(referenceDate.minusDays(daysAgo.toLong()))
    }
}
