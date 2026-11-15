package dev.ishant.easyniddle

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration

public typealias NiddleEntryProvider = (NavKey) -> NavEntry<NavKey>

@Composable
public fun rememberNiddleBackStack(
    savedStateConfiguration: SavedStateConfiguration,
    startDestination: NavKey,
): NavBackStack<NavKey> = rememberNavBackStack(savedStateConfiguration, startDestination)

/**
 * Navigation 3 host. The app owns the back stack, keeping navigation explicit
 * and easy to test. Use NiddleController for named stack operations.
 */
@Composable
public fun NiddleNavHost(
    backStack: NavBackStack<NavKey>,
    entryProvider: NiddleEntryProvider,
    onBack: (() -> Unit)? = null,
) {
    NavDisplay(
        backStack = backStack,
        onBack = onBack ?: {
            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider,
    )
}

/** Builds entries using Navigation 3's official entry-provider DSL. */
public fun niddleEntries(
    builder: EntryProviderScope<NavKey>.() -> Unit,
): NiddleEntryProvider = entryProvider(builder = builder)

/**
 * Builds entries and checks that every requested route belongs to [graph].
 * Unknown route types fail immediately with a clear registration error.
 */
public fun niddleEntries(
    graph: NiddleGraph,
    builder: EntryProviderScope<NavKey>.() -> Unit,
): NiddleEntryProvider {
    val resolveEntry = entryProvider(builder = builder)
    return { key ->
        graph.requireContains(key)
        resolveEntry(key)
    }
}
