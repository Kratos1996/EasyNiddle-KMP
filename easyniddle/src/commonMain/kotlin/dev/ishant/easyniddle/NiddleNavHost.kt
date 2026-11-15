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

/** Compose transition applied when a destination is pushed onto the back stack. */
public typealias NiddleTransitionSpec =
    AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform

/** Compose transition applied during pop and predictive-back navigation. */
public typealias NiddlePredictivePopTransitionSpec =
    AnimatedContentTransitionScope<Scene<NavKey>>.(swipeEdge: Int) -> ContentTransform

/**
 * Entry lookup function consumed by [NiddleNavHost].
 *
 * Usually create this with [niddleEntries] so destinations are registered using
 * Navigation 3's official entry-provider DSL.
 */
public typealias NiddleEntryProvider = (NavKey) -> NavEntry<NavKey>

/** A balanced horizontal push transition with a subtle fade. */
public val niddleSlideInTransition: NiddleTransitionSpec = {
    slideInHorizontally { width -> width / 4 } + fadeIn() togetherWith
        slideOutHorizontally { width -> -width / 6 } + fadeOut()
}

/** A balanced horizontal pop transition with a subtle fade. */
public val niddleSlidePopTransition: NiddleTransitionSpec = {
    slideInHorizontally { width -> -width / 6 } + fadeIn() togetherWith
        slideOutHorizontally { width -> width / 4 } + fadeOut()
}

/**
 * Fade transition suitable for Navigation 3 predictive-back rendering.
 *
 * Navigation 3 drives the gesture lifecycle; this lambda only defines the
 * visual transform.
 */
public val niddlePredictiveFadeTransition: NiddlePredictivePopTransitionSpec = { _ ->
    fadeIn() togetherWith fadeOut()
}

/**
 * Remembers a saveable Navigation 3 back stack for this Compose host.
 *
 * Every concrete route that can enter the stack must have a serializer
 * registered in [savedStateConfiguration]. The stack remains owned by the
 * caller and can be passed to [NiddleController] and [NiddleNavHost].
 *
 * @param savedStateConfiguration polymorphic route serializer configuration.
 * @param startDestination initial route shown when no saved stack is restored.
 */
@Composable
public fun rememberNiddleBackStack(
    savedStateConfiguration: SavedStateConfiguration,
    startDestination: NavKey,
): NavBackStack<NavKey> = rememberNavBackStack(savedStateConfiguration, startDestination)

/**
 * Displays a caller-owned Navigation 3 stack.
 *
 * Navigation 3 remains responsible for entry rendering, saveable entry state,
 * transitions, and predictive-back gesture handling. If [onBack] is null, the
 * host removes the top entry when the stack contains more than its root.
 *
 * @param backStack saveable Navigation 3 stack created or managed by the app.
 * @param entryProvider maps route keys to their UI entries.
 * @param onBack optional app-specific back action; null uses the default pop.
 * @param transitionSpec visual transform for forward navigation.
 * @param popTransitionSpec visual transform for ordinary pop navigation.
 * @param predictivePopTransitionSpec visual transform for predictive back.
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

/**
 * Builds an entry provider using Navigation 3's official DSL.
 *
 * Use this overload when route ownership is validated elsewhere or the app
 * intentionally does not maintain a [NiddleGraph].
 */
public fun niddleEntries(
    builder: EntryProviderScope<NavKey>.() -> Unit,
): NiddleEntryProvider = entryProvider(builder = builder)

/**
 * Builds an entry provider and validates that every requested route belongs
 * to [graph]. Unknown route types fail before Navigation 3 resolves an entry.
 *
 * @param graph immutable graph containing all routes that this provider serves.
 * @param builder Navigation 3 entry declarations for those routes.
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
