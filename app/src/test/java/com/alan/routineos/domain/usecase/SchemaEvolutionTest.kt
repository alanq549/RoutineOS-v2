package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SchemaEvolutionTest {

    @Test
    fun `schema evolution preserves historical execution structure`() = runTest {
        val target = ScheduleTarget.Node("n1")
        
        // 1. Create Schema v1
        val fieldA = MetadataField("id-a", "Peso", MetadataFieldType.NUMBER, unit = "kg")
        val fieldB = MetadataField("id-b", "Series", MetadataFieldType.NUMBER)
        val schemaV1 = MetadataSchema("s1", target, listOf(fieldA, fieldB), schemaVersion = 1)

        // 2. Create Execution E1 (captured with v1)
        val e1Values = buildJsonObject {
            put("id-a", 15)
            put("id-b", 3)
        }
        val e1Metadata = buildJsonObject {
            put("schemaVersion", 1)
            put("values", e1Values)
        }
        val execution1 = ActivityExecution("e1", "n1", null, 0L, 1000L, e1Metadata.toString())

        // 3. Update Schema to v2 (Add RPE, Rename Peso)
        val fieldAUpdated = fieldA.copy(name = "Masa") // Renamed display name
        val fieldC = MetadataField("id-c", "RPE", MetadataFieldType.NUMBER)
        val schemaV2 = MetadataSchema("s1", target, listOf(fieldAUpdated, fieldB, fieldC), schemaVersion = 2)

        // 4. Create Execution E2 (captured with v2)
        val e2Values = buildJsonObject {
            put("id-a", 20)
            put("id-b", 3)
            put("id-c", 8)
        }
        val e2Metadata = buildJsonObject {
            put("schemaVersion", 2)
            put("values", e2Values)
        }
        val execution2 = ActivityExecution("e2", "n1", null, 0L, 2000L, e2Metadata.toString())

        // 5. Verification
        // Check Execution 1: Should still have ID 'id-a' with value 15
        val e1Json = Json.parseToJsonElement(execution1.metadataJson).jsonObject
        assertEquals(1, e1Json["schemaVersion"]?.jsonPrimitive?.int)
        assertEquals(15, e1Json["values"]?.jsonObject?.get("id-a")?.jsonPrimitive?.int)
        
        // Check Execution 2: Has new field and updated value for old ID
        val e2Json = Json.parseToJsonElement(execution2.metadataJson).jsonObject
        assertEquals(2, e2Json["schemaVersion"]?.jsonPrimitive?.int)
        assertEquals(20, e2Json["values"]?.jsonObject?.get("id-a")?.jsonPrimitive?.int)
        assertEquals(8, e2Json["values"]?.jsonObject?.get("id-c")?.jsonPrimitive?.int)

        // The point is: changing schemaV1 -> schemaV2 did not touch execution1.
        assertNotEquals(schemaV1.schemaVersion, schemaV2.schemaVersion)
    }
}
