package com.sss.healthcare

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.dataStore by preferencesDataStore(name = "sleep_records")

class SleepDataManager(private val context: Context) {

    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    fun getSleepData(date: LocalDate): Flow<SleepRecord?> {
        val dateStr = date.toString()
        val bedtimeKey = stringPreferencesKey("bedtime_$dateStr")
        val wakeupKey = stringPreferencesKey("wakeup_$dateStr")

        return context.dataStore.data.map { preferences ->
            val bedtimeStr = preferences[bedtimeKey]
            val wakeupStr = preferences[wakeupKey]

            if (bedtimeStr != null && wakeupStr != null) {
                SleepRecord(
                    bedtime = LocalTime.parse(bedtimeStr, timeFormatter),
                    wakeupTime = LocalTime.parse(wakeupStr, timeFormatter)
                )
            } else {
                null
            }
        }
    }

    suspend fun saveSleepData(date: LocalDate, record: SleepRecord) {
        val dateStr = date.toString()
        val bedtimeKey = stringPreferencesKey("bedtime_$dateStr")
        val wakeupKey = stringPreferencesKey("wakeup_$dateStr")

        context.dataStore.edit { preferences ->
            preferences[bedtimeKey] = record.bedtime.format(timeFormatter)
            preferences[wakeupKey] = record.wakeupTime.format(timeFormatter)
        }
    }
}

data class SleepRecord(
    val bedtime: LocalTime,
    val wakeupTime: LocalTime
)
