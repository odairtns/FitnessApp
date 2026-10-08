package com.aksoit.myfitnessapp.ui.navigation

import kotlinx.serialization.Serializable

sealed interface ScreenRoute {
    // 4 Pilares Principais (BottomBar)
    @Serializable data object Home : ScreenRoute
    @Serializable data object Workouts : ScreenRoute
    @Serializable data object History : ScreenRoute
    @Serializable data object Tools : ScreenRoute

    // Telas Secundárias e Modais
    @Serializable data class TemplateEditor(val templateId: Long? = null) : ScreenRoute
    @Serializable data class ExerciseLibrary(val selectMode: Boolean = false) : ScreenRoute
    @Serializable data class WorkoutPlayer(val templateId: Long) : ScreenRoute
    @Serializable data class SessionDetail(val sessionId: Long) : ScreenRoute
    @Serializable data class XmlExchange(val isImport: Boolean = true) : ScreenRoute
    @Serializable data object PersonalRecords : ScreenRoute
}
