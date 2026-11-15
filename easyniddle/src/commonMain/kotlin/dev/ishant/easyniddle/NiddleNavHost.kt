package dev.ishant.easyniddle

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration

/** Resolves a Navigation 3 key to its entry. */
public typealias NiddleEntryProvider = (NavKey) -> NavEntry<NavKey>

/** Navigation 3 host with EasyNiddle's saveable entry-state decorator. */
@Composable
public fun NiddleNavHost(
    startDestination: NavKey,
    savedStateConfiguration: SavedStateConfiguration,
    entryProvider: NiddleEntryProvider,
    onBack: (() -> Unit)? = null,
) {
    val backStack = rememberNavBackStack(savedStateConfiguration, startDestination)
    NavDisplay(
        backStack = backStack,
        onBack = onBack ?: { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider,
    )
}

/** Build a Navigation 3 entry provider using its official typed entry DSL. */
public fun niddleEntries(
    builder: EntryProviderScope<NavKey>.() -> Unit,
): NiddleEntryProvider = entryProvider(builder = builder)
