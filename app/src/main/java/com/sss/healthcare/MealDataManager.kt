package com.sss.healthcare

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.mealDataStore by preferencesDataStore(name = "meal_records")

class MealDataManager(private val context: Context) {
    fun getMealData(date: LocalDate): Flow<MealRecord?> {
        val dateStr = date.toString()
        return context.mealDataStore.data.map { preferences ->
            val breakfast = preferences[stringPreferencesKey("breakfast_$dateStr")]
            val lunch = preferences[stringPreferencesKey("lunch_$dateStr")]
            val dinner = preferences[stringPreferencesKey("dinner_$dateStr")]
            val snack = preferences[stringPreferencesKey("snack_$dateStr")]
            val nightSnack = preferences[stringPreferencesKey("nightSnack_$dateStr")]

            if (breakfast != null || lunch != null || dinner != null || snack != null || nightSnack != null) {
                MealRecord(
                    breakfast = breakfast ?: "",
                    lunch = lunch ?: "",
                    dinner = dinner ?: "",
                    snack = snack ?: "",
                    nightSnack = nightSnack ?: ""
                )
            } else null
        }
    }

    suspend fun saveMealData(date: LocalDate, record: MealRecord) {
        val dateStr = date.toString()
        context.mealDataStore.edit { preferences ->
            preferences[stringPreferencesKey("breakfast_$dateStr")] = record.breakfast
            preferences[stringPreferencesKey("lunch_$dateStr")] = record.lunch
            preferences[stringPreferencesKey("dinner_$dateStr")] = record.dinner
            preferences[stringPreferencesKey("snack_$dateStr")] = record.snack
            preferences[stringPreferencesKey("nightSnack_$dateStr")] = record.nightSnack
        }
    }
}

data class MealRecord(
    val breakfast: String = "",
    val lunch: String = "",
    val dinner: String = "",
    val snack: String = "",
    val nightSnack: String = ""
)
