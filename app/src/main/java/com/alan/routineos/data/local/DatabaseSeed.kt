package com.alan.routineos.data.local

import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.*
import com.alan.routineos.domain.model.MetadataField
import com.alan.routineos.domain.model.MetadataFieldType
import com.alan.routineos.domain.model.ScheduleRuleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

/**
 * Seeding utility for development phase.
 */
object DatabaseSeed {

    private val json = Json { ignoreUnknownKeys = true }

    fun seedAll(
        defDao: ActivityDefinitionDao,
        nodeDao: ActivityNodeDao,
        ruleDao: ScheduleRuleDao,
        metaDao: MetadataSchemaDao,
        systemDao: SystemDao
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (systemDao.getSystemsList().isNotEmpty()) return@launch

            // 0. SYSTEMS
            val sysHealth = "sys_health"; val sysCareer = "sys_career"; val sysLife = "sys_life"
            systemDao.upsertSystem(SystemEntity(sysHealth, "Salud", "Cuerpo y mente", "fitness_center", "#5AF0B3"))
            systemDao.upsertSystem(SystemEntity(sysCareer, "Carrera", "Estudios", "school", "#A4C8FF"))
            systemDao.upsertSystem(SystemEntity(sysLife, "Personal", "Vida diaria", "wb_sunny", "#DBD1FF"))

            // 1. DESPERTAR
            val defWake = "def_wake"
            defDao.insertActivityDefinition(ActivityDefinitionEntity(defWake, "Rutina Mañana", "Inicio del día", sysLife))
            val nWake = "n_wake"; val nShower = "n_shower"
            nodeDao.insertNode(ActivityNodeEntity(nWake, defWake, null, 0, "Despertar"))
            nodeDao.insertNode(ActivityNodeEntity(nShower, defWake, nWake, 0, "Bañarme"))
            ruleDao.insertRule(createRule(nWake, "NODE", setOf(1,2,3,4,5,6,7), 330)) // 5:30
            ruleDao.insertRule(createRule(nShower, "NODE", setOf(1,2,3,4,5,6,7), 345)) // 5:45

            // 2. UNIVERSIDAD
            val defUniv = "def_univ"
            defDao.insertActivityDefinition(ActivityDefinitionEntity(defUniv, "Universidad", "Horario Semestral", sysCareer))
            val nUniv = "n_univ"; val nProg = "n_prog"; val nDb = "n_db"; val nIA = "n_ia"
            nodeDao.insertNode(ActivityNodeEntity(nUniv, defUniv, null, 0, "Clases"))
            nodeDao.insertNode(ActivityNodeEntity(nProg, defUniv, nUniv, 0, "Programación"))
            nodeDao.insertNode(ActivityNodeEntity(nDb, defUniv, nUniv, 1, "Bases de Datos"))
            nodeDao.insertNode(ActivityNodeEntity(nIA, defUniv, nUniv, 2, "Inteligencia Artificial"))
            
            // Rules: Mon, Wed, Thu (7-14) | Tue (7-13) | Fri (7-15)
            ruleDao.insertRule(createRule(nUniv, "NODE", setOf(1, 3, 4), 420, 840))
            ruleDao.insertRule(createRule(nUniv, "NODE", setOf(2), 420, 780))
            ruleDao.insertRule(createRule(nUniv, "NODE", setOf(5), 420, 900))
            
            insertContextSchema(metaDao, nProg, "Profesor", "Grace Hopper")
            insertContextSchema(metaDao, nDb, "Profesor", "Edgar Codd")
            insertContextSchema(metaDao, nIA, "Profesor", "Alan Turing")

            // 3. GIMNASIO
            val defGym = "def_gym"
            defDao.insertActivityDefinition(ActivityDefinitionEntity(defGym, "Gimnasio", "Rutina de pesas", sysHealth))
            val nPush = "n_push"; val nPull = "n_pull"; val nLegs = "n_legs"
            nodeDao.insertNode(ActivityNodeEntity(nPush, defGym, null, 0, "Push Day"))
            nodeDao.insertNode(ActivityNodeEntity(nPull, defGym, null, 1, "Pull Day"))
            nodeDao.insertNode(ActivityNodeEntity(nLegs, defGym, null, 2, "Leg Day"))

            // Schedules: Mon-Thu 16:00 | Fri 17:00
            ruleDao.insertRule(createRule(nPush, "NODE", setOf(1), 960)) 
            ruleDao.insertRule(createRule(nPull, "NODE", setOf(2), 960))
            ruleDao.insertRule(createRule(nLegs, "NODE", setOf(3, 6), 960))
            ruleDao.insertRule(createRule(nPush, "NODE", setOf(4), 1080)) // Thu special
            ruleDao.insertRule(createRule(nPull, "NODE", setOf(5), 1020)) // Fri special

            // Gym Exercises & Metadata
            seedGymExercises(nodeDao, metaDao, defGym, nPush, nPull, nLegs)

            // 4. NOCHE
            val nEat = "n_eat"; val nWork = "n_work"; val nSleep = "n_sleep"
            nodeDao.insertNode(ActivityNodeEntity(nEat, defWake, null, 1, "Comer"))
            nodeDao.insertNode(ActivityNodeEntity(nWork, defWake, null, 2, "Tarea"))
            nodeDao.insertNode(ActivityNodeEntity(nSleep, defWake, null, 3, "Dormir"))
            ruleDao.insertRule(createRule(nEat, "NODE", setOf(1,2,3,4), 1080, 1200)) // 18:00
            ruleDao.insertRule(createRule(nWork, "NODE", setOf(1,2,3,4), 1200, 1320)) // 20:00
            ruleDao.insertRule(createRule(nSleep, "NODE", setOf(1,2,3,4,5,6,7), 1395)) // 23:15
        }
    }

    private suspend fun seedGymExercises(nodeDao: ActivityNodeDao, metaDao: MetadataSchemaDao, defId: String, nPush: String, nPull: String, nLegs: String) {
        // Push
        val exercisesPush = listOf("n_lat" to (3 to 12), "n_front" to (3 to 12), "n_bench" to (3 to 10), "n_inc" to (3 to 10), "n_dips" to (3 to 10), "n_skull" to (3 to 12))
        val titlesPush = listOf("Elevaciones Lateral", "Elevaciones Frontal", "Press Banca", "Press Inclinado", "Fondos", "Rompe Craneos")
        val weightsPush = listOf(10, 10, 30, 15, 60, 15)
        
        exercisesPush.forEachIndexed { i, ex ->
            nodeDao.insertNode(ActivityNodeEntity(ex.first, defId, nPush, i, titlesPush[i]))
            insertExerciseSchema(metaDao, ex.first, ex.second.first, ex.second.second, weightsPush[i])
        }

        // Pull
        val exercisesPull = listOf("n_row" to (3 to 10), "n_curl" to (3 to 10), "n_hammer" to (3 to 10), "n_fore" to (3 to 10))
        val titlesPull = listOf("Remos con barra", "Curl de Bicep", "Curl Martillo", "Curl Antebrazo")
        val weightsPull = listOf(35, 20, 10, 15)
        
        exercisesPull.forEachIndexed { i, ex ->
            nodeDao.insertNode(ActivityNodeEntity(ex.first, defId, nPull, i, titlesPull[i]))
            insertExerciseSchema(metaDao, ex.first, ex.second.first, ex.second.second, weightsPull[i])
        }

        // Legs
        val exercisesLegs = listOf("n_squat" to (3 to 10), "n_f_curl" to (3 to 10), "n_ext" to (3 to 10), "n_hip" to (3 to 8), "n_calf" to (3 to 15))
        val titlesLegs = listOf("Sentadilla Libre", "Curl Femoral", "Extensiones Cuadripcep", "Hip Thrust", "Gemelos")
        val weightsLegs = listOf(30, 40, 45, 40, 0)
        
        exercisesLegs.forEachIndexed { i, ex ->
            nodeDao.insertNode(ActivityNodeEntity(ex.first, defId, nLegs, i, titlesLegs[i]))
            insertExerciseSchema(metaDao, ex.first, ex.second.first, ex.second.second, weightsLegs[i])
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

    private suspend fun insertExerciseSchema(dao: MetadataSchemaDao, nodeId: String, series: Int, reps: Int, weight: Int) {
        val fields = listOf(
            MetadataField(UUID.randomUUID().toString(), "Series", MetadataFieldType.NUMBER, defaultValue = series.toString(), required = true),
            MetadataField(UUID.randomUUID().toString(), "Repeticiones", MetadataFieldType.NUMBER, defaultValue = reps.toString(), required = true),
            MetadataField(UUID.randomUUID().toString(), "Peso", MetadataFieldType.NUMBER, unit = "kg", defaultValue = weight.toString(), required = true)
        )
        dao.insertSchema(MetadataSchemaEntity(UUID.randomUUID().toString(), nodeId, "NODE", json.encodeToString(fields), 1))
    }

    private suspend fun insertContextSchema(dao: MetadataSchemaDao, nodeId: String, name: String, value: String) {
        val fields = listOf(MetadataField(UUID.randomUUID().toString(), name, MetadataFieldType.TEXT, isReadOnly = true, defaultValue = value))
        dao.insertSchema(MetadataSchemaEntity(UUID.randomUUID().toString(), nodeId, "NODE", json.encodeToString(fields), 1))
    }
}
