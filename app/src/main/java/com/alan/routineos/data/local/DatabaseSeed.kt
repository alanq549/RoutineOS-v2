package com.alan.routineos.data.local

import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

/**
 * Seeding utility for development phase.
 * WARNING: This is only for testing hierarchical features.
 */
object DatabaseSeed {

    fun seedHierarchy(
        definitionDaoProvider: Provider<ActivityDefinitionDao>,
        nodeDaoProvider: Provider<ActivityNodeDao>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val defDao = definitionDaoProvider.get()
            val nodeDao = nodeDaoProvider.get()

            val defId = "demo_univ"

            // Check if specifically the demo activity exists
            if (defDao.getActivityDefinitionById(defId) != null) return@launch
            
            // 1. Root Activity
            defDao.insertActivityDefinition(
                ActivityDefinitionEntity(
                    id = defId,
                    title = "Universidad (Demo Jerárquica)",
                    description = "Estructura de 3 niveles para validar EC-RE-001"
                )
            )

            // 2. Depth 0 Nodes
            val dbId = "n_db"
            val netId = "n_net"
            val softId = "n_soft"

            nodeDao.insertNode(ActivityNodeEntity(dbId, defId, null, 0, "Bases de Datos"))
            nodeDao.insertNode(ActivityNodeEntity(netId, defId, null, 1, "Redes"))
            nodeDao.insertNode(ActivityNodeEntity(softId, defId, null, 2, "Ingeniería de Software"))

            // 3. Depth 1 Nodes
            val sqlId = "n_sql"
            val nosqlId = "n_nosql"
            nodeDao.insertNode(ActivityNodeEntity(sqlId, defId, dbId, 0, "SQL Lab"))
            nodeDao.insertNode(ActivityNodeEntity(nosqlId, defId, dbId, 1, "NoSQL Lab"))

            val vlanId = "n_vlan"
            val eigrpId = "n_eigrp"
            nodeDao.insertNode(ActivityNodeEntity(vlanId, defId, netId, 0, "VLAN Setup"))
            nodeDao.insertNode(ActivityNodeEntity(eigrpId, defId, netId, 1, "EIGRP Config"))

            // 4. Depth 2 Nodes (The "SQL Lab" children)
            nodeDao.insertNode(ActivityNodeEntity("n_join", defId, sqlId, 0, "Consultas JOIN complejas"))
            nodeDao.insertNode(ActivityNodeEntity("n_index", defId, sqlId, 1, "Optimización de Índices"))
        }
    }
}
