package com.aksoit.myfitnessapp.application.xml

import com.aksoit.myfitnessapp.data.mapper.toEnumOr
import com.aksoit.myfitnessapp.domain.model.BlockType
import com.aksoit.myfitnessapp.domain.model.GroupType
import com.aksoit.myfitnessapp.domain.model.InvalidTemplateException
import com.aksoit.myfitnessapp.domain.model.PlannedSet
import com.aksoit.myfitnessapp.domain.model.SideMode
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.domain.model.WorkoutExercise
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.UUID
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

object WorkoutTemplateXmlParser {

    fun parse(xmlString: String): WorkoutTemplate {
        val dbFactory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            try {
                setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            } catch (_: Exception) {
                // Algumas engines podem não suportar flags específicas
            }
            isXIncludeAware = false
            isExpandEntityReferences = false
        }

        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(InputSource(StringReader(xmlString)))
        doc.documentElement.normalize()

        val root = doc.documentElement
        val templateUuid = root.getAttribute("templateUuid").ifBlank { UUID.randomUUID().toString() }

        var name = ""
        var modality = WorkoutModality.STRENGTH
        var protocol = WorkoutProtocol.STANDARD_STRENGTH
        var description = ""
        var estimatedDuration: Int? = null

        val metadataList = root.getElementsByTagName("metadata")
        if (metadataList.length > 0) {
            val metaNode = metadataList.item(0) as Element
            name = getChildText(metaNode, "name").orEmpty()
            modality = getChildText(metaNode, "modality").toEnumOr(WorkoutModality.STRENGTH)
            protocol = getChildText(metaNode, "protocol").toEnumOr(WorkoutProtocol.STANDARD_STRENGTH)
            description = getChildText(metaNode, "description").orEmpty()
            estimatedDuration = getChildText(metaNode, "estimatedDurationSeconds")?.toIntOrNull()
        }

        if (name.isBlank()) throw InvalidTemplateException("Arquivo XML não contém nome de treino válido.")

        val blocks = mutableListOf<WorkoutBlock>()
        val blocksParentList = root.getElementsByTagName("blocks")
        if (blocksParentList.length > 0) {
            val blocksParent = blocksParentList.item(0) as Element
            val blockNodes = blocksParent.childNodes
            var blockIdx = 0
            for (i in 0 until blockNodes.length) {
                val bNode = blockNodes.item(i)
                if (bNode.nodeType == Node.ELEMENT_NODE && bNode.nodeName == "block") {
                    val bElement = bNode as Element
                    val pos = bElement.getAttribute("position").toIntOrNull() ?: blockIdx
                    val bType = bElement.getAttribute("type").toEnumOr(BlockType.WORK)
                    val bName = bElement.getAttribute("name").ifBlank { "Bloco ${pos + 1}" }
                    val rounds = getChildText(bElement, "rounds")?.toIntOrNull() ?: 1
                    val workDuration = getChildText(bElement, "workDurationSeconds")?.toIntOrNull()
                    val restDuration = getChildText(bElement, "restDurationSeconds")?.toIntOrNull()

                    val exercises = mutableListOf<WorkoutExercise>()
                    val exercisesParentList = bElement.getElementsByTagName("exercises")
                    if (exercisesParentList.length > 0) {
                        val exParent = exercisesParentList.item(0) as Element
                        val exNodes = exParent.childNodes
                        var exIdx = 0
                        for (j in 0 until exNodes.length) {
                            val eNode = exNodes.item(j)
                            if (eNode.nodeType == Node.ELEMENT_NODE && eNode.nodeName == "exercise") {
                                val eElement = eNode as Element
                                val key = eElement.getAttribute("key").takeIf { it.isNotBlank() }
                                val exName = eElement.getAttribute("name").ifBlank { "Exercício" }
                                val groupType = eElement.getAttribute("groupType").toEnumOr(GroupType.NONE)
                                val groupId = eElement.getAttribute("groupId").takeIf { it.isNotBlank() }

                                val sets = mutableListOf<PlannedSet>()
                                val setsParentList = eElement.getElementsByTagName("sets")
                                if (setsParentList.length > 0) {
                                    val setsParent = setsParentList.item(0) as Element
                                    val setNodes = setsParent.childNodes
                                    var setIdx = 1
                                    for (k in 0 until setNodes.length) {
                                        val sNode = setNodes.item(k)
                                        if (sNode.nodeType == Node.ELEMENT_NODE && sNode.nodeName == "set") {
                                            val sElement = sNode as Element
                                            val sNum = sElement.getAttribute("number").toIntOrNull() ?: setIdx
                                            val targetReps = sElement.getAttribute("targetReps").toIntOrNull()
                                            val targetLoad = sElement.getAttribute("targetLoadKg").toDoubleOrNull()
                                            val targetDur = sElement.getAttribute("targetDurationSeconds").toIntOrNull()
                                            val restSec = sElement.getAttribute("restSeconds").toIntOrNull()
                                            val sideMode = sElement.getAttribute("sideMode").toEnumOr(SideMode.BILATERAL)

                                            sets.add(
                                                PlannedSet(
                                                    setNumber = sNum,
                                                    targetReps = targetReps,
                                                    targetLoadKg = targetLoad,
                                                    targetDurationSeconds = targetDur,
                                                    restSeconds = restSec,
                                                    sideMode = sideMode
                                                )
                                            )
                                            setIdx++
                                        }
                                    }
                                }

                                exercises.add(
                                    WorkoutExercise(
                                        exerciseId = null,
                                        exerciseNameCustom = exName,
                                        position = exIdx,
                                        groupId = groupId,
                                        groupType = groupType,
                                        plannedSets = sets,
                                        resolvedName = exName,
                                        resolvedStableKey = key
                                    )
                                )
                                exIdx++
                            }
                        }
                    }

                    blocks.add(
                        WorkoutBlock(
                            blockType = bType,
                            position = pos,
                            name = bName,
                            rounds = rounds,
                            workDurationSeconds = workDuration,
                            restDurationSeconds = restDuration,
                            exercises = exercises
                        )
                    )
                    blockIdx++
                }
            }
        }

        return WorkoutTemplate(
            templateUuid = templateUuid,
            name = name,
            modality = modality,
            protocol = protocol,
            description = description,
            estimatedDurationSeconds = estimatedDuration,
            blocks = blocks,
            createdAtEpochMs = System.currentTimeMillis(),
            updatedAtEpochMs = System.currentTimeMillis()
        )
    }

    private fun getChildText(parent: Element, tagName: String): String? {
        val list = parent.getElementsByTagName(tagName)
        return if (list.length > 0) list.item(0).textContent?.trim() else null
    }
}
