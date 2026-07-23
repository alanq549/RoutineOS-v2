package com.alan.routineos.data.mapper

import com.alan.routineos.data.local.entities.RoutineEntity
import com.alan.routineos.data.local.entities.TaskEntity
import com.alan.routineos.domain.model.Routine
import com.alan.routineos.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineMapperTest {

    @Test
    fun `RoutineEntity toDomain maps correctly`() {
        val entity = RoutineEntity(id = "1", title = "Title", description = "Desc")
        val domain = entity.toDomain()
        
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.description, domain.description)
    }

    @Test
    fun `Routine toEntity maps correctly`() {
        val domain = Routine(id = "1", title = "Title", description = "Desc")
        val entity = domain.toEntity()
        
        assertEquals(domain.id, entity.id)
        assertEquals(domain.title, entity.title)
        assertEquals(domain.description, entity.description)
    }

    @Test
    fun `TaskEntity toDomain maps correctly`() {
        val entity = TaskEntity(id = "1", routineId = "r1", title = "Task")
        val domain = entity.toDomain()
        
        assertEquals(entity.id, domain.id)
        assertEquals(entity.routineId, domain.routineId)
        assertEquals(entity.title, domain.title)
    }

    @Test
    fun `Task toEntity maps correctly`() {
        val domain = Task(id = "1", routineId = "r1", title = "Task")
        val entity = domain.toEntity()
        
        assertEquals(domain.id, entity.id)
        assertEquals(domain.routineId, entity.routineId)
        assertEquals(domain.title, entity.title)
    }

    @Test
    fun `List of RoutineEntity toDomain maps correctly`() {
        val entities = listOf(
            RoutineEntity(id = "1", title = "T1", description = "D1"),
            RoutineEntity(id = "2", title = "T2", description = "D2")
        )
        val domainList = entities.map { it.toDomain() }

        assertEquals(entities.size, domainList.size)
        assertEquals(entities[0].id, domainList[0].id)
        assertEquals(entities[1].id, domainList[1].id)
    }
}
