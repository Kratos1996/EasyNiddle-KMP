package dev.ishant.easyniddle

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration

/**
 * Transition used when a destination is pushed onto the back stack.
 */
public typealias NiddleTransitionSpec =
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform

/**
 * Transition used when a destination is popped, including a predictive-back gesture.
 */
public typealias NiddlePredictivePopTransitionSpec =
    AnimatedContentTransitionScope<Scene<NavKey>>.(swipeEdge: Int) -> ContentTransform

public typealias NiddleEntryProvider = (NavKey) -> NavEntry<NavKey>

/**
 * A balanced horizontal transition for forward navigation.
 */
public val niddleSlideInTransition: NiddleTransitionSpec = {
    slideInHorizontally { width -> width / 4 } + fadeIn() togetherWith
        slideOutHorizontally { width -> -width / 6 } + fadeOut()
}

/**
 * A balanced horizontal transition for back navigation.
 */
public val niddleSlidePopTransition: NiddleTransitionSpec = {
    slideInHorizontally { width -> -width / 6 } + fadeIn() togetherWith
        slideOutHorizontally { width -> width / 4 } + fadeOut()
}

/**
 * A fade transition that can be driven interactively by Navigation 3 predictive back.
 */
public val niddlePredictiveFadeTransition: NiddlePredictivePopTransitionSpec = { _ ->
    fadeIn() togetherWith fadeOut()
}

@Composable
public fun rememberNiddleBackStack(
    savedStateConfiguration: SavedStateConfiguration,
    startDestination: NavKey,
): NavBackStack<NavKey> = rememberNavBackStack(savedStateConfiguration, startDestination)

/**
 * Navigation 3 host with configurable push, pop, and predictive-back transitions.
 *
 * Predictive back gesture progress and completion are handled by Navigation 3. The supplied
 * [predictivePopTransitionSpec] defines the visual transform used during that gesture.
 */
@Composable
public fun NiddleNavHost(
    backStack: NavBackStack<NavKey>,
    entryProvider: NiddleEntryProvider,
    onBack: (() -> Unit)? = null,
    transitionSpec: NiddleTransitionSpec = niddleSlideInTransition,
    popTransitionSpec: NiddleTransitionSpec = niddleSlidePopTransition,
    predictivePopTransitionSpec: NiddlePredictivePopTransitionSpec = niddlePredictiveFadeTransition,
) {
    NavDisplay(
        backStack = backStack,
        onBack = onBack ?: {
            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        transitionSpec = transitionSpec,
        popTransitionSpec = popTransitionSpec,
        predictivePopTransitionSpec = predictivePopTransitionSpec,
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
