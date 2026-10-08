package com.aksoit.myfitnessapp.data.repository

import com.aksoit.myfitnessapp.data.local.entity.PersonalRecordEntity
import com.aksoit.myfitnessapp.data.local.entity.SetFactRow
import com.aksoit.myfitnessapp.domain.model.LoadUnitConverter
import com.aksoit.myfitnessapp.domain.model.PersonalRecordType
import com.aksoit.myfitnessapp.domain.model.estimateOneRepMaxKg

/**
 * Recalcula recordes pessoais a partir dos fatos históricos (fonte da verdade = performed_sets).
 * Em empate, prevalece o primeiro registro cronológico.
 */
class PersonalRecordCalculator {

    fun compute(facts: List<SetFactRow>): List<PersonalRecordEntity> {
        if (facts.isEmpty()) return emptyList()
        val ordered = facts.sortedBy { it.loggedAtEpochMs }
        val name = ordered.first().exerciseNameSnapshot
        val exerciseId = ordered.lastOrNull { it.exerciseId != null }?.exerciseId
        val records = mutableListOf<PersonalRecordEntity>()

        ordered.filter { (it.actualLoadKg ?: 0.0) > 0.0 }
            .fold<SetFactRow, SetFactRow?>(null) { best, f -> if (best == null || f.actualLoadKg!! > best.actualLoadKg!!) f else best }
            ?.let { f ->
                records += PersonalRecordEntity(
                    exerciseId = exerciseId,
                    exerciseNameSnapshot = name,
                    recordType = PersonalRecordType.MAX_LOAD.name,
                    value = f.actualLoadKg!!,
                    reps = f.actualReps,
                    loadKg = f.actualLoadKg,
                    sessionId = f.sessionId,
                    achievedAtEpochMs = f.loggedAtEpochMs
                )
            }

        ordered.filter { (it.actualLoadKg ?: 0.0) > 0.0 && (it.actualReps ?: 0) > 0 }
            .map { it to estimateOneRepMaxKg(it.actualLoadKg!!, it.actualReps!!) }
            .fold<Pair<SetFactRow, Double>, Pair<SetFactRow, Double>?>(null) { best, p ->
                if (best == null || p.second > best.second) p else best
            }
            ?.let { (f, oneRm) ->
                records += PersonalRecordEntity(
                    exerciseId = exerciseId,
                    exerciseNameSnapshot = name,
                    recordType = PersonalRecordType.ESTIMATED_1RM.name,
                    value = LoadUnitConverter.round1(oneRm),
                    reps = f.actualReps,
                    loadKg = f.actualLoadKg,
                    sessionId = f.sessionId,
                    achievedAtEpochMs = f.loggedAtEpochMs
                )
            }

        ordered.filter { (it.actualReps ?: 0) > 0 }
            .fold<SetFactRow, SetFactRow?>(null) { best, f -> if (best == null || f.actualReps!! > best.actualReps!!) f else best }
            ?.let { f ->
                records += PersonalRecordEntity(
                    exerciseId = exerciseId,
                    exerciseNameSnapshot = name,
                    recordType = PersonalRecordType.MAX_REPS.name,
                    value = f.actualReps!!.toDouble(),
                    reps = f.actualReps,
                    loadKg = f.actualLoadKg,
                    sessionId = f.sessionId,
                    achievedAtEpochMs = f.loggedAtEpochMs
                )
            }
        return records
    }
}
