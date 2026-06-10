package com.sss.healthcare

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.exerciseDataStore by preferencesDataStore(name = "exercise_records")

class ExerciseDataManager(private val context: Context) {
    fun getExerciseData(date: LocalDate): Flow<ExerciseRecord?> {
        val dateStr = date.toString()
        return context.exerciseDataStore.data.map { preferences ->
            val steps = preferences[stringPreferencesKey("steps_$dateStr")]
            val runTime = preferences[stringPreferencesKey("runTime_$dateStr")]
            val runDist = preferences[stringPreferencesKey("runDist_$dateStr")]
            val cycleTime = preferences[stringPreferencesKey("cycleTime_$dateStr")]
            val cycleDist = preferences[stringPreferencesKey("cycleDist_$dateStr")]

            if (steps != null) {
                ExerciseRecord(steps, runTime ?: "0", runDist ?: "0", cycleTime ?: "0", cycleDist ?: "0")
            } else null
        }
    }

    suspend fun saveExerciseData(date: LocalDate, record: ExerciseRecord) {
        val dateStr = date.toString()
        context.exerciseDataStore.edit { preferences ->
            preferences[stringPreferencesKey("steps_$dateStr")] = record.steps
            preferences[stringPreferencesKey("runTime_$dateStr")] = record.runTime
            preferences[stringPreferencesKey("runDist_$dateStr")] = record.runDistance
            preferences[stringPreferencesKey("cycleTime_$dateStr")] = record.cycleTime
            preferences[stringPreferencesKey("cycleDist_$dateStr")] = record.cycleDistance
        }
    }
}

data class ExerciseRecord(
    val steps: String = "0",
    val runTime: String = "0",
    val runDistance: String = "0",
    val cycleTime: String = "0",
    val cycleDistance: String = "0"
)
