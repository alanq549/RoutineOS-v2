package com.alan.routineos.data.mapper

import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityNode
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityMapperTest {

    @Test
    fun `ActivityDefinitionEntity toDomain maps correctly`() {
        val entity = ActivityDefinitionEntity(id = "1", title = "Title", description = "Desc")
        val domain = entity.toDomain()
        
        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.description, domain.description)
    }

    @Test
    fun `ActivityDefinition toEntity maps correctly`() {
        val domain = ActivityDefinition(id = "1", title = "Title", description = "Desc")
        val entity = domain.toEntity()
        
        assertEquals(domain.id, entity.id)
        assertEquals(domain.title, entity.title)
        assertEquals(domain.description, entity.description)
    }

    @Test
    fun `ActivityNodeEntity toDomain maps correctly`() {
        val entity = ActivityNodeEntity(id = "1", activityDefinitionId = "ad1", title = "Node")
        val domain = entity.toDomain()
        
        assertEquals(entity.id, domain.id)
        assertEquals(entity.activityDefinitionId, domain.activityDefinitionId)
        assertEquals(entity.title, domain.title)
    }

    @Test
    fun `ActivityNode toEntity maps correctly`() {
        val domain = ActivityNode(id = "1", activityDefinitionId = "ad1", title = "Node")
        val entity = domain.toEntity()
        
        assertEquals(domain.id, entity.id)
        assertEquals(domain.activityDefinitionId, entity.activityDefinitionId)
        assertEquals(domain.title, entity.title)
    }

    @Test
    fun `List of ActivityDefinitionEntity toDomain maps correctly`() {
        val entities = listOf(
            ActivityDefinitionEntity(id = "1", title = "T1", description = "D1"),
            ActivityDefinitionEntity(id = "2", title = "T2", description = "D2")
        )
        
        val domainList = entities.map { it.toDomain() }
        
        assertEquals(entities.size, domainList.size)
        assertEquals(entities[0].title, domainList[0].title)
        assertEquals(entities[1].title, domainList[1].title)
    }
}
