package com.aksoit.myfitnessapp

import com.aksoit.myfitnessapp.data.local.entity.SetFactRow
import com.aksoit.myfitnessapp.data.repository.PersonalRecordCalculator
import com.aksoit.myfitnessapp.domain.model.PersonalRecordType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalRecordCalculatorTest {

    private val calculator = PersonalRecordCalculator()

    @Test
    fun computesMaxLoadMaxRepsAndMaxVolumeWithoutEstimated1Rm() {
        val facts = listOf(
            SetFactRow(
                sessionId = 10,
                exerciseId = 100,
                exerciseNameSnapshot = "Supino Reto",
                actualLoadKg = 80.0,
                actualReps = 10,
                loggedAtEpochMs = 1000L
            ),
            SetFactRow(
                sessionId = 10,
                exerciseId = 100,
                exerciseNameSnapshot = "Supino Reto",
                actualLoadKg = 100.0,
                actualReps = 5,
                loggedAtEpochMs = 2000L
            ),
            SetFactRow(
                sessionId = 10,
                exerciseId = 100,
                exerciseNameSnapshot = "Supino Reto",
                actualLoadKg = 60.0,
                actualReps = 15,
                loggedAtEpochMs = 3000L
            )
        )

        val prs = calculator.compute(facts)

        // Deve conter MAX_LOAD, MAX_REPS, MAX_VOLUME
        val types = prs.map { it.recordType }
        assertTrue(types.contains(PersonalRecordType.MAX_LOAD.name))
        assertTrue(types.contains(PersonalRecordType.MAX_REPS.name))
        assertTrue(types.contains(PersonalRecordType.MAX_VOLUME.name))
        // INV-009: Estimated 1RM não faz parte da V1
        assertFalse(types.contains("ESTIMATED_1RM"))

        val maxLoad = prs.first { it.recordType == PersonalRecordType.MAX_LOAD.name }
        assertEquals(100.0, maxLoad.value, 0.001)

        val maxReps = prs.first { it.recordType == PersonalRecordType.MAX_REPS.name }
        assertEquals(15.0, maxReps.value, 0.001)

        // Set 1: 80 * 10 = 800; Set 2: 100 * 5 = 500; Set 3: 60 * 15 = 900
        val maxVolume = prs.first { it.recordType == PersonalRecordType.MAX_VOLUME.name }
        assertEquals(900.0, maxVolume.value, 0.001)
    }
}
