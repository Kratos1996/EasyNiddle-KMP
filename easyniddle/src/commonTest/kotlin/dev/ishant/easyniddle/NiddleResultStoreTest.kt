package dev.ishant.easyniddle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private object ResultKeys {
    val selectedUser = NiddleResultKey<String>("selected-user")
    val selectedTab = NiddleResultKey<Int>("selected-tab")
}

class NiddleResultStoreTest {
    @Test
    fun consumesResultOnlyOnce() {
        val store = NiddleResultStore()
        store.set(ResultKeys.selectedUser, "user-42")

        assertEquals("user-42", store.consume(ResultKeys.selectedUser))
        assertNull(store.consume(ResultKeys.selectedUser))
        assertFalse(store.hasResult(ResultKeys.selectedUser))
    }

    @Test
    fun keepsDifferentKeysIsolated() {
        val store = NiddleResultStore()
        store.set(ResultKeys.selectedUser, "user-42")
        store.set(ResultKeys.selectedTab, 2)

        assertEquals(2, store.consume(ResultKeys.selectedTab))
        assertEquals("user-42", store.consume(ResultKeys.selectedUser))
    }

    @Test
    fun replacesPendingValueForSameKey() {
        val store = NiddleResultStore()
        store.set(ResultKeys.selectedTab, 1)
        store.set(ResultKeys.selectedTab, 3)

        assertEquals(3, store.consume(ResultKeys.selectedTab))
        assertNull(store.consume(ResultKeys.selectedTab))
    }

    @Test
    fun independentlyCreatedKeysDoNotShareValues() {
        val store = NiddleResultStore()
        val firstKey = NiddleResultKey<String>("profile")
        val secondKey = NiddleResultKey<String>("profile")
        store.set(firstKey, "first")

        assertNull(store.consume(secondKey))
        assertEquals("first", store.consume(firstKey))
    }

    @Test
    fun clearRemovesOnePendingResult() {
        val store = NiddleResultStore()
        store.set(ResultKeys.selectedUser, "user-42")
        store.set(ResultKeys.selectedTab, 2)

        store.clear(ResultKeys.selectedUser)

        assertFalse(store.hasResult(ResultKeys.selectedUser))
        assertTrue(store.hasResult(ResultKeys.selectedTab))
        assertEquals(2, store.consume(ResultKeys.selectedTab))
    }

    @Test
    fun clearAllRemovesEveryPendingResult() {
        val store = NiddleResultStore()
        store.set(ResultKeys.selectedUser, "user-42")
        store.set(ResultKeys.selectedTab, 2)

        store.clearAll()

        assertFalse(store.hasResult(ResultKeys.selectedUser))
        assertFalse(store.hasResult(ResultKeys.selectedTab))
        assertNull(store.consume(ResultKeys.selectedUser))
        assertNull(store.consume(ResultKeys.selectedTab))
    }

    @Test
    fun missingResultReturnsNull() {
        assertNull(NiddleResultStore().consume(ResultKeys.selectedUser))
    }
}
