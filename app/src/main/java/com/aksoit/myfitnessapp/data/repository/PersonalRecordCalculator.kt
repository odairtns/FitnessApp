package com.aksoit.myfitnessapp.data.repository

import com.aksoit.myfitnessapp.data.local.entity.PersonalRecordEntity
import com.aksoit.myfitnessapp.data.local.entity.SetFactRow
import com.aksoit.myfitnessapp.domain.model.PersonalRecordType

/**
 * Recalcula recordes pessoais a partir dos fatos históricos (fonte da verdade = performed_sets).
 * Em empate, prevalece o primeiro registro cronológico.
 * V1: Registra somente fatos observados (MAX_LOAD, MAX_REPS, MAX_VOLUME).
 * Estimated 1RM não faz parte da V1 (COR-007).
 */
class PersonalRecordCalculator {

    fun compute(facts: List<SetFactRow>): List<PersonalRecordEntity> {
        if (facts.isEmpty()) return emptyList()
        val ordered = facts.sortedBy { it.loggedAtEpochMs }
        val name = ordered.first().exerciseNameSnapshot
        val exerciseId = ordered.lastOrNull { it.exerciseId != null }?.exerciseId
        val records = mutableListOf<PersonalRecordEntity>()

        // 1. MAX_LOAD (Maior carga em kg)
        ordered.filter { (it.actualLoadKg ?: 0.0) > 0.0 }
            .fold<SetFactRow, SetFactRow?>(null) { best, f ->
                if (best == null || f.actualLoadKg!! > best.actualLoadKg!!) f else best
            }
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

        // 2. MAX_REPS (Maior número de repetições em uma série)
        ordered.filter { (it.actualReps ?: 0) > 0 }
            .fold<SetFactRow, SetFactRow?>(null) { best, f ->
                if (best == null || f.actualReps!! > best.actualReps!!) f else best
            }
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

        // 3. MAX_VOLUME (Maior volume em uma série = carga * reps)
        ordered.filter { (it.actualLoadKg ?: 0.0) > 0.0 && (it.actualReps ?: 0) > 0 }
            .map { it to (it.actualLoadKg!! * it.actualReps!!) }
            .fold<Pair<SetFactRow, Double>, Pair<SetFactRow, Double>?>(null) { best, p ->
                if (best == null || p.second > best.second) p else best
            }
            ?.let { (f, volume) ->
                records += PersonalRecordEntity(
                    exerciseId = exerciseId,
                    exerciseNameSnapshot = name,
                    recordType = PersonalRecordType.MAX_VOLUME.name,
                    value = volume,
                    reps = f.actualReps,
                    loadKg = f.actualLoadKg,
                    sessionId = f.sessionId,
                    achievedAtEpochMs = f.loggedAtEpochMs
                )
            }

        return records
    }
}
