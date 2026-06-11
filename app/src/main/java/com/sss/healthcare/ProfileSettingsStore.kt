package com.sss.healthcare

import android.content.Context

data class ProfileSettings(
    val selectedDisease: String = "고혈압",
    val medicationReminderEnabled: Boolean = true
)

object ProfileSettingsStore {
    private const val PREF_NAME = "profile_settings"
    private const val KEY_SELECTED_DISEASE = "selected_disease"
    private const val KEY_MEDICATION_REMINDER = "medication_reminder"

    fun load(context: Context): ProfileSettings {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return ProfileSettings(
            selectedDisease = preferences.getString(
                KEY_SELECTED_DISEASE,
                ProfileSettings().selectedDisease
            ) ?: ProfileSettings().selectedDisease,
            medicationReminderEnabled = preferences.getBoolean(
                KEY_MEDICATION_REMINDER,
                ProfileSettings().medicationReminderEnabled
            )
        )
    }

    fun save(context: Context, settings: ProfileSettings) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_DISEASE, settings.selectedDisease)
            .putBoolean(KEY_MEDICATION_REMINDER, settings.medicationReminderEnabled)
            .apply()
    }
}
