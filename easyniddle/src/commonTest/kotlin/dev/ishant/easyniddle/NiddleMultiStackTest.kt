package dev.ishant.easyniddle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NiddleMultiStackTest {
    private fun create() = NiddleMultiStack(
        roots = linkedMapOf(
            "home" to "home-root",
            "search" to "search-root",
            "profile" to "profile-root",
        ),
        initialTab = "home",
    )

    @Test
    fun eachTabKeepsItsOwnBackStack() {
        val navigation = create()
        navigation.navigate("home-detail")

        navigation.selectTab("search")
        navigation.navigate("search-result")

        navigation.selectTab("home")
        assertEquals(listOf("home-root", "home-detail"), navigation.currentStack)

        navigation.selectTab("search")
        assertEquals(listOf("search-root", "search-result"), navigation.currentStack)
    }

    @Test
    fun backOnlyPopsActiveTab() {
        val navigation = create()
        navigation.navigate("home-detail")
        navigation.selectTab("profile")

        assertFalse(navigation.back())
        assertEquals(listOf("home-root", "home-detail"), navigation.stackFor("home"))
        assertEquals(listOf("profile-root"), navigation.currentStack)
    }

    @Test
    fun reselectCanPopOnlySelectedTabToRoot() {
        val navigation = create()
        navigation.navigate("home-detail")
        navigation.selectTab("search")
        navigation.navigate("search-result")

        navigation.selectTab("search", popToRootOnReselect = true)

        assertEquals(listOf("search-root"), navigation.currentStack)
        assertEquals(listOf("home-root", "home-detail"), navigation.stackFor("home"))
    }

    @Test
    fun singleTopAvoidsAdjacentDuplicate() {
        val navigation = create()
        navigation.navigate("home-detail")
        navigation.navigateSingleTop("home-detail")
        assertEquals(listOf("home-root", "home-detail"), navigation.currentStack)
    }

    @Test
    fun popToUsesLastMatchingDestinationAndPreservesRoot() {
        val navigation = create()
        navigation.navigate("details")
        navigation.navigate("details")
        navigation.navigate("edit")

        assertTrue(navigation.popTo("details"))
        assertEquals(listOf("home-root", "details", "details"), navigation.currentStack)
        assertFalse(navigation.popTo("home-root", inclusive = true))
    }
}
