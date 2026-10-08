package com.aksoit.myfitnessapp.application.xml

import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate

object WorkoutTemplateXmlSerializer {

    private const val XML_NS = "urn:fitness-app:workout-template"
    private const val SCHEMA_VERSION = "3.2"

    fun serialize(template: WorkoutTemplate): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<workoutTemplate templateUuid=\"").append(escapeXml(template.templateUuid))
            .append("\" schemaVersion=\"").append(SCHEMA_VERSION)
            .append("\" xmlns=\"").append(XML_NS).append("\">\n")

        // Metadata
        sb.append("    <metadata>\n")
        sb.append("        <name>").append(escapeXml(template.name)).append("</name>\n")
        sb.append("        <modality>").append(escapeXml(template.modality.name)).append("</modality>\n")
        sb.append("        <protocol>").append(escapeXml(template.protocol.name)).append("</protocol>\n")
        sb.append("        <description>").append(escapeXml(template.description)).append("</description>\n")
        template.estimatedDurationSeconds?.let {
            sb.append("        <estimatedDurationSeconds>").append(it).append("</estimatedDurationSeconds>\n")
        }
        sb.append("    </metadata>\n")

        // Blocks
        sb.append("    <blocks>\n")
        template.blocks.sortedBy { it.position }.forEach { block ->
            sb.append("        <block position=\"").append(block.position)
                .append("\" type=\"").append(escapeXml(block.blockType.name))
                .append("\" name=\"").append(escapeXml(block.name)).append("\">\n")

            sb.append("            <rounds>").append(block.rounds).append("</rounds>\n")
            block.workDurationSeconds?.let {
                sb.append("            <workDurationSeconds>").append(it).append("</workDurationSeconds>\n")
            }
            block.restDurationSeconds?.let {
                sb.append("            <restDurationSeconds>").append(it).append("</restDurationSeconds>\n")
            }

            sb.append("            <exercises>\n")
            block.exercises.sortedBy { it.position }.forEach { exercise ->
                sb.append("                <exercise")
                exercise.resolvedStableKey?.let { sb.append(" key=\"").append(escapeXml(it)).append("\"") }
                sb.append(" name=\"").append(escapeXml(exercise.displayName)).append("\"")
                sb.append(" groupType=\"").append(escapeXml(exercise.groupType.name)).append("\"")
                exercise.groupId?.let { sb.append(" groupId=\"").append(escapeXml(it)).append("\"") }
                sb.append(">\n")

                sb.append("                    <sets>\n")
                exercise.plannedSets.sortedBy { it.setNumber }.forEach { set ->
                    sb.append("                        <set number=\"").append(set.setNumber).append("\"")
                    set.targetReps?.let { sb.append(" targetReps=\"").append(it).append("\"") }
                    set.targetLoadKg?.let { sb.append(" targetLoadKg=\"").append(it).append("\"") }
                    set.targetDurationSeconds?.let { sb.append(" targetDurationSeconds=\"").append(it).append("\"") }
                    set.restSeconds?.let { sb.append(" restSeconds=\"").append(it).append("\"") }
                    sb.append(" sideMode=\"").append(escapeXml(set.sideMode.name)).append("\"/>\n")
                }
                sb.append("                    </sets>\n")
                sb.append("                </exercise>\n")
            }
            sb.append("            </exercises>\n")
            sb.append("        </block>\n")
        }
        sb.append("    </blocks>\n")
        sb.append("</workoutTemplate>")

        return sb.toString()
    }

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
