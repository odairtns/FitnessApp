package com.aksoit.myfitnessapp

import com.aksoit.myfitnessapp.application.xml.WorkoutTemplateXmlParser
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WorkoutTemplateXmlTest {

    @Test
    fun parseValidXmlCorrectly() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <workoutTemplate 
                templateUuid="c8b4f172-2e5b-4c74-9f01-9a7c64a5d84e"
                schemaVersion="3.2"
                xmlns="urn:fitness-app:workout-template">
                
                <metadata>
                    <name>Treino Superior Hipertrofia</name>
                    <modality>STRENGTH</modality>
                    <protocol>STANDARD_STRENGTH</protocol>
                    <description>Foco em peito, ombros e tríceps.</description>
                    <estimatedDurationSeconds>3600</estimatedDurationSeconds>
                </metadata>

                <blocks>
                    <block position="1" type="WORK" name="Supino Reto">
                        <rounds>1</rounds>
                        <exercises>
                            <exercise key="barbell-bench-press" name="Supino Reto com Barra">
                                <sets>
                                    <set number="1" targetReps="10" targetLoadKg="80.0" restSeconds="90" sideMode="BILATERAL"/>
                                    <set number="2" targetReps="8" targetLoadKg="85.0" restSeconds="90" sideMode="BILATERAL"/>
                                </sets>
                            </exercise>
                        </exercises>
                    </block>
                </blocks>
            </workoutTemplate>
        """.trimIndent()

        val template = WorkoutTemplateXmlParser.parse(xml)

        assertEquals("c8b4f172-2e5b-4c74-9f01-9a7c64a5d84e", template.templateUuid)
        assertEquals("Treino Superior Hipertrofia", template.name)
        assertEquals(WorkoutModality.STRENGTH, template.modality)
        assertEquals(WorkoutProtocol.STANDARD_STRENGTH, template.protocol)
        assertEquals(1, template.blocks.size)
        assertEquals("Supino Reto", template.blocks[0].name)
        assertEquals(1, template.blocks[0].exercises.size)
        assertEquals(2, template.blocks[0].exercises[0].plannedSets.size)
        assertEquals(80.0, template.blocks[0].exercises[0].plannedSets[0].targetLoadKg)
    }
}
