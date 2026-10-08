package com.aksoit.myfitnessapp.domain.model

import kotlin.math.roundToLong

/**
 * Unidade de exibição de carga. O armazenamento é SEMPRE em kg (Regra Inegociável 3).
 */
enum class WeightUnit(val suffix: String) {
    KG("kg"),
    LB("lb");
}

object LoadUnitConverter {
    const val LB_PER_KG = 2.20462

    /** Converte um valor digitado na unidade de exibição para kg canônico. */
    fun toKg(value: Double, unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG -> value
        WeightUnit.LB -> value / LB_PER_KG
    }

    /** Converte kg canônico para a unidade de exibição, arredondado a 1 casa decimal. */
    fun fromKg(kg: Double, unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG -> round1(kg)
        WeightUnit.LB -> round1(kg * LB_PER_KG)
    }

    fun round1(value: Double): Double = (value * 10.0).roundToLong() / 10.0
}

/** Preferências do atleta (feedback independente e unidade visual). */
data class UserPreferences(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val soundEnabled: Boolean = true,
    val voiceEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val loadStepKg: Double = 2.0
)
