package com.alan.routineos.data.local

import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.domain.model.MetadataField
import com.alan.routineos.domain.model.MetadataFieldType
import com.alan.routineos.domain.model.ScheduleRuleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import javax.inject.Provider

/**
 * Seeding utility for development phase.
 */
object DatabaseSeed {

    private val json = Json { ignoreUnknownKeys = true }

    fun seedAll(
        defDao: ActivityDefinitionDao,
        nodeDao: ActivityNodeDao,
        ruleDao: ScheduleRuleDao,
        metaDao: MetadataSchemaDao
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if already seeded to avoid duplicates
            if (defDao.getDefinitionsList().isNotEmpty()) return@launch

            // 1. DESPERTAR
            val defWake = "def_wake"
            defDao.insertActivityDefinition(ActivityDefinitionEntity(defWake, "Despertar", "Rutina matutina básica"))
            
            val nodeWake = "n_wake"
            val nodeShower = "n_shower"
            nodeDao.insertNode(ActivityNodeEntity(nodeWake, defWake, null, 0, "Despertar"))
            nodeDao.insertNode(ActivityNodeEntity(nodeShower, defWake, nodeWake, 0, "Bañarme"))
            
            // Daily at 05:30 and 05:45
            ruleDao.insertRule(createRule(nodeWake, "NODE", setOf(1,2,3,4,5,6,7), 330)) 
            ruleDao.insertRule(createRule(nodeShower, "NODE", setOf(1,2,3,4,5,6,7), 345))

            // 2. UNIVERSIDAD
            val defUniv = "def_univ"
            defDao.insertActivityDefinition(ActivityDefinitionEntity(defUniv, "Universidad", "Horario académico semestre actual"))
            val nodeUniv = "n_univ"
            nodeDao.insertNode(ActivityNodeEntity(nodeUniv, defUniv, null, 0, "Clases Presenciales"))
            
            // Add actual subjects as children of Clases Presenciales
            nodeDao.insertNode(ActivityNodeEntity("n_prog", defUniv, nodeUniv, 0, "Programación"))
            nodeDao.insertNode(ActivityNodeEntity("n_db_sub", defUniv, nodeUniv, 1, "Bases de Datos"))
            nodeDao.insertNode(ActivityNodeEntity("n_ia", defUniv, nodeUniv, 2, "IA"))
            
            // Mon, Wed, Thu: 07:00 - 14:00 (420 - 840 min)
            ruleDao.insertRule(createRule(nodeUniv, "NODE", setOf(1, 3, 4), 420, 840))
            // Tue: 07:00 - 13:00 (420 - 780 min)
            ruleDao.insertRule(createRule(nodeUniv, "NODE", setOf(2), 420, 780))
            // Fri: 07:00 - 15:00 (420 - 900 min)
            ruleDao.insertRule(createRule(nodeUniv, "NODE", setOf(5), 420, 900))

            metaDao.insertSchema(MetadataSchemaEntity(
                id = UUID.randomUUID().toString(),
                targetId = nodeUniv,
                targetType = "NODE",
                schemaVersion = 1,
                fieldsJson = json.encodeToString(listOf(
                    MetadataField(UUID.randomUUID().toString(), "Profesor", MetadataFieldType.TEXT, isReadOnly = true, defaultValue = "Dr. Armando Casas")
                ))
            ))

            // 3. GIMNASIO
            val defGym = "def_gym"
            defDao.insertActivityDefinition(ActivityDefinitionEntity(defGym, "Gimnasio", "Programa de fuerza y volumen"))
            
            // Nodes (Structure)
            val nPush = "n_push"; val nPull = "n_pull"; val nLegs = "n_legs"
            nodeDao.insertNode(ActivityNodeEntity(nPush, defGym, null, 0, "Push Day (Pecho/Hombro)"))
            nodeDao.insertNode(ActivityNodeEntity(nPull, defGym, null, 1, "Pull Day (Espalda/Bicep)"))
            nodeDao.insertNode(ActivityNodeEntity(nLegs, defGym, null, 2, "Leg Day (Pierna)"))

            // Exercises
            val nBench = "n_bench"; val nRow = "n_row"; val nSquat = "n_squat"
            val nSkull = "n_skull"; val nCurl = "n_curl"
            
            // Mapping Rules (as requested by user)
            // Mon: Push
            ruleDao.insertRule(createRule(nPush, "NODE", setOf(1), 960)) 
            // Tue: Pull
            ruleDao.insertRule(createRule(nPull, "NODE", setOf(2), 960))
            // Wed & Sat: Legs
            ruleDao.insertRule(createRule(nLegs, "NODE", setOf(3, 6), 960))
            // Thu: Chest & Back mix (Special schedule for sub-nodes)
            ruleDao.insertRule(createRule(nBench, "NODE", setOf(4), 960))
            ruleDao.insertRule(createRule(nRow, "NODE", setOf(4), 1000))
            // Fri: Arms mix (Bicep/Tricep)
            ruleDao.insertRule(createRule(nCurl, "NODE", setOf(5), 1020))
            ruleDao.insertRule(createRule(nSkull, "NODE", setOf(5), 1040))

            // Definitions (Leaf nodes data)
            nodeDao.insertNode(ActivityNodeEntity(nBench, defGym, nPush, 0, "Press Banca"))
            nodeDao.insertNode(ActivityNodeEntity("n_incline", defGym, nPush, 1, "Press Inclinado"))
            nodeDao.insertNode(ActivityNodeEntity("n_dips", defGym, nPush, 2, "Fondos"))
            nodeDao.insertNode(ActivityNodeEntity(nSkull, defGym, nPush, 3, "Rompe Cráneos"))
            nodeDao.insertNode(ActivityNodeEntity("n_lat", defGym, nPush, 4, "Elevaciones Laterales"))
            nodeDao.insertNode(ActivityNodeEntity("n_front", defGym, nPush, 5, "Elevaciones Frontales"))

            nodeDao.insertNode(ActivityNodeEntity(nRow, defGym, nPull, 0, "Remos con barra"))
            nodeDao.insertNode(ActivityNodeEntity(nCurl, defGym, nPull, 1, "Curl de Bicep"))
            nodeDao.insertNode(ActivityNodeEntity("n_hammer", defGym, nPull, 2, "Curl Martillo"))
            nodeDao.insertNode(ActivityNodeEntity("n_forearm", defGym, nPull, 3, "Antebrazo"))

            nodeDao.insertNode(ActivityNodeEntity(nSquat, defGym, nLegs, 0, "Sentadilla Libre"))
            nodeDao.insertNode(ActivityNodeEntity("n_f_curl", defGym, nLegs, 1, "Curl Femoral"))
            nodeDao.insertNode(ActivityNodeEntity("n_ext", defGym, nLegs, 2, "Extensiones Cuádriceps"))
            nodeDao.insertNode(ActivityNodeEntity("n_hip", defGym, nLegs, 3, "Hip Thrust"))
            nodeDao.insertNode(ActivityNodeEntity("n_calf", defGym, nLegs, 4, "Gemelos"))

            // 5. GYM METADATA (Schemas with Default Values)
            // Push
            insertExerciseSchema(metaDao, nBench, 3, 10, 30)
            insertExerciseSchema(metaDao, "n_incline", 3, 10, 15)
            insertExerciseSchema(metaDao, "n_dips", 3, 10, 60)
            insertExerciseSchema(metaDao, nSkull, 3, 12, 15)
            insertExerciseSchema(metaDao, "n_lat", 3, 12, 10)
            insertExerciseSchema(metaDao, "n_front", 3, 12, 10)
            
            // Pull
            insertExerciseSchema(metaDao, nRow, 3, 10, 35)
            insertExerciseSchema(metaDao, nCurl, 3, 10, 20)
            insertExerciseSchema(metaDao, "n_hammer", 3, 10, 10)
            insertExerciseSchema(metaDao, "n_forearm", 3, 10, 15)
            
            // Legs
            insertExerciseSchema(metaDao, nSquat, 3, 10, 30)
            insertExerciseSchema(metaDao, "n_f_curl", 3, 10, 40)
            insertExerciseSchema(metaDao, "n_ext", 3, 10, 45)
            insertExerciseSchema(metaDao, "n_hip", 3, 8, 40)
            insertExerciseSchema(metaDao, "n_calf", 3, 15, 0)

            // 4. COMIDA & TAREA
            val defLife = "def_life"
            defDao.insertActivityDefinition(ActivityDefinitionEntity(defLife, "Vida Diaria", "Actividades de soporte"))
            
            val nEat = "n_eat"; val nHome = "n_home"; val nSleep = "n_sleep"
            nodeDao.insertNode(ActivityNodeEntity(nEat, defLife, null, 0, "Comer"))
            nodeDao.insertNode(ActivityNodeEntity(nHome, defLife, null, 1, "Hacer Tarea"))
            nodeDao.insertNode(ActivityNodeEntity(nSleep, defLife, null, 2, "Dormir"))

            // Mon-Thu 18:00 - 20:00
            ruleDao.insertRule(createRule(nEat, "NODE", setOf(1,2,3,4), 1080, 1200))
            // Mon-Thu 20:00 - 22:00
            ruleDao.insertRule(createRule(nHome, "NODE", setOf(1,2,3,4), 1200, 1320))
            // Daily 23:15
            ruleDao.insertRule(createRule(nSleep, "NODE", setOf(1,2,3,4,5,6,7), 1395))
        }
    }

    private fun createRule(targetId: String, type: String, days: Set<Int>, start: Int, end: Int? = null) = ScheduleRuleEntity(
        id = UUID.randomUUID().toString(),
        targetId = targetId,
        targetType = type,
        type = ScheduleRuleType.FIXED_DAYS.name,
        daysOfWeek = days.sorted().joinToString(","),
        startTime = start,
        endTime = end
    )

    private suspend fun insertExerciseSchema(
        dao: MetadataSchemaDao,
        nodeId: String,
        series: Int,
        reps: Int,
        weight: Int
    ) {
        val fields = listOf(
            MetadataField(UUID.randomUUID().toString(), "Series", MetadataFieldType.NUMBER, defaultValue = series.toString()),
            MetadataField(UUID.randomUUID().toString(), "Repeticiones", MetadataFieldType.NUMBER, defaultValue = reps.toString()),
            MetadataField(UUID.randomUUID().toString(), "Peso", MetadataFieldType.NUMBER, unit = "kg", defaultValue = weight.toString())
        )
        dao.insertSchema(MetadataSchemaEntity(
            id = UUID.randomUUID().toString(),
            targetId = nodeId,
            targetType = "NODE",
            schemaVersion = 1,
            fieldsJson = Json.encodeToString(fields)
        ))
    }
}
