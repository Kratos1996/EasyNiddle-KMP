package dev.ishant.easyniddle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackStackOperationsTest {
    @Test fun popKeepsRoot() {
        val stack = mutableListOf("home")
        assertFalse(BackStackOperations.pop(stack))
        assertEquals(listOf("home"), stack)
    }

    @Test fun popRemovesTop() {
        val stack = mutableListOf("home", "profile")
        assertTrue(BackStackOperations.pop(stack))
        assertEquals(listOf("home"), stack)
    }

    @Test fun popToKeepsTargetByDefault() {
        val stack = mutableListOf("home", "search", "detail", "edit")
        assertTrue(BackStackOperations.popTo(stack, "search"))
        assertEquals(listOf("home", "search"), stack)
    }

    @Test fun inclusivePopCannotRemoveRoot() {
        val stack = mutableListOf("home", "search")
        assertFalse(BackStackOperations.popTo(stack, "home", inclusive = true))
        assertEquals(listOf("home", "search"), stack)
    }

    @Test fun singleTopAvoidsAdjacentDuplicate() {
        val stack = mutableListOf("home", "profile")
        BackStackOperations.navigateSingleTop(stack, "profile")
        assertEquals(listOf("home", "profile"), stack)
    }
}
