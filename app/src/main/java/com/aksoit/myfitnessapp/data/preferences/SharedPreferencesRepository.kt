package com.aksoit.myfitnessapp.data.preferences

import android.content.Context
import androidx.core.content.edit
import com.aksoit.myfitnessapp.domain.model.UserPreferences
import com.aksoit.myfitnessapp.domain.model.WeightUnit
import com.aksoit.myfitnessapp.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Preferências locais (offline) em SharedPreferences. */
class SharedPreferencesRepository(context: Context) : PreferencesRepository {

    private val prefs = context.applicationContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
    private val _preferences = MutableStateFlow(read())
    override val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun read(): UserPreferences = UserPreferences(
        weightUnit = if (prefs.getString(KEY_UNIT, WeightUnit.KG.name) == WeightUnit.LB.name) WeightUnit.LB else WeightUnit.KG,
        soundEnabled = prefs.getBoolean(KEY_SOUND, true),
        voiceEnabled = prefs.getBoolean(KEY_VOICE, true),
        hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
        loadStepKg = prefs.getFloat(KEY_STEP, 2f).toDouble()
    )

    override fun update(transform: (UserPreferences) -> UserPreferences) {
        _preferences.update { current ->
            val next = transform(current)
            prefs.edit {
                putString(KEY_UNIT, next.weightUnit.name)
                putBoolean(KEY_SOUND, next.soundEnabled)
                putBoolean(KEY_VOICE, next.voiceEnabled)
                putBoolean(KEY_HAPTICS, next.hapticsEnabled)
                putFloat(KEY_STEP, next.loadStepKg.toFloat())
            }
            next
        }
    }

    override fun isSampleDataSeeded(): Boolean = prefs.getBoolean(KEY_SEEDED, false)

    override fun markSampleDataSeeded() = prefs.edit { putBoolean(KEY_SEEDED, true) }

    private companion object {
        const val KEY_UNIT = "weight_unit"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_VOICE = "voice_enabled"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_STEP = "load_step_kg"
        const val KEY_SEEDED = "sample_data_seeded"
    }
}
