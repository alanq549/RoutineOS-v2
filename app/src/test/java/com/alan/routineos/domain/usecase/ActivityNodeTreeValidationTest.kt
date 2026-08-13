package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ActivityNode
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityNodeTreeValidationTest {

    private val validator = ValidateActivityNodeUseCase()

    @Test
    fun `self-parent is rejected`() {
        val node = ActivityNode("1", "act1", "1", 0, "Self")
        val error = validator(node, emptyList())
        assertEquals(ActivityNodeValidationError.SelfParent, error)
    }

    @Test
    fun `ancestor cycle is rejected`() {
        val allNodes = listOf(
            ActivityNode("1", "act1", "2", 0, "A"),
            ActivityNode("2", "act1", "1", 0, "B")
        )
        val node = allNodes[0]
        val error = validator(node, allNodes)
        assertEquals(ActivityNodeValidationError.AncestorCycle, error)
    }

    @Test
    fun `cross-definition parent is rejected`() {
        val allNodes = listOf(
            ActivityNode("parent1", "act_other", null, 0, "Other Parent")
        )
        val node = ActivityNode("1", "act1", "parent1", 0, "Node")
        val error = validator(node, allNodes)
        assertEquals(ActivityNodeValidationError.CrossDefinitionParent, error)
    }

    @Test
    fun `nonexistent parent is rejected`() {
        val node = ActivityNode("1", "act1", "nonexistent", 0, "Node")
        val error = validator(node, emptyList())
        assertEquals(ActivityNodeValidationError.NonexistentParent, error)
    }
}
