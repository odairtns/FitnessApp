package com.aksoit.myfitnessapp.domain.repository

import com.aksoit.myfitnessapp.domain.model.UserPreferences
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    val preferences: StateFlow<UserPreferences>
    fun update(transform: (UserPreferences) -> UserPreferences)
    fun isSampleDataSeeded(): Boolean
    fun markSampleDataSeeded()
}
