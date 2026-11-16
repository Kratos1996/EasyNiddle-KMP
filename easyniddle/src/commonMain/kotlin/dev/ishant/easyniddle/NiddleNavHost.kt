package dev.ishant.easyniddle

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

/** Entry lookup function consumed by [NiddleNavHost]. */
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

/** Fade transition suitable for Navigation 3 predictive-back rendering. */
public val niddlePredictiveFadeTransition: NiddlePredictivePopTransitionSpec = { _ ->
    fadeIn() togetherWith fadeOut()
}

/** Remembers a saveable Navigation 3 back stack for this Compose host. */
@Composable
public fun rememberNiddleBackStack(
    savedStateConfiguration: SavedStateConfiguration,
    startDestination: NavKey,
): NavBackStack<NavKey> = rememberNavBackStack(savedStateConfiguration, startDestination)

/**
 * Displays a caller-owned Navigation 3 stack.
 *
 * This is the lower-level API. For the familiar one-call setup, use the
 * [NiddleRoute] overload that accepts `startDestination` and a screen DSL.
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
 * Familiar EasyNiddle host: define a start route and register screens in one DSL.
 *
 * Example:
 * ```kotlin
 * NiddleNavHost<AppRoute>(startDestination = AppRoute.Home) {
 *     screen<AppRoute.Home> { HomeScreen() }
 *     screen<AppRoute.Details> { route -> DetailsScreen(route.id) }
 * }
 * ```
 *
 * Provide [savedStateConfiguration] for the route family by registering each
 * concrete route serializer with `route(...)`.
 *
 * @param startDestination initial route when no saved stack is restored.
 * @param navigator command facade; defaults to a host-scoped remembered instance.
 * @param onBack called when back is requested at the root. Default is a no-op.
 * @param onFinish called when [NiddleNavigator.finish] is requested.
 * @param onNavigateCustom receives a custom route command emitted by the navigator.
 * @param onResult receives one-shot results emitted by [NiddleNavigator.navigateBackWithResult].
 */
@Composable
public fun <Route : NiddleRoute> NiddleNavHost(
    startDestination: Route,
    navigator: NiddleNavigator = remember { NiddleNavigator() },
    savedStateConfiguration: SavedStateConfiguration,
    onBack: () -> Unit = {},
    onFinish: () -> Unit = {},
    onNavigateCustom: (Route) -> Unit = {},
    onResult: (Any) -> Unit = {},
    transitionSpec: NiddleTransitionSpec = niddleSlideInTransition,
    popTransitionSpec: NiddleTransitionSpec = niddleSlidePopTransition,
    predictivePopTransitionSpec: NiddlePredictivePopTransitionSpec = niddlePredictiveFadeTransition,
    builder: NiddleNavGraphBuilder<Route>.() -> Unit,
) {
    val backStack = rememberNiddleBackStack(
        savedStateConfiguration = savedStateConfiguration,
        startDestination = startDestination,
    )
    val entryProvider = remember(builder) {
        @Suppress("UNCHECKED_CAST")
        NiddleNavGraphBuilder<Route>().apply(builder).build() as (NavKey) -> NavEntry<NavKey>
    }

    LaunchedEffect(navigator, backStack) {
        navigator.collectEvents { event ->
            when (event) {
                is Navigator.NavigateRoute -> backStack.add(event.route)
                is Navigator.NavigateWithPopUp -> {
                    popStackTo(backStack, event.popUpToRoute, event.inclusive)
                    backStack.add(event.route)
                }
                is Navigator.NavigateToBackstackScreenAsSingleTop -> {
                    val index = backStack.indexOfLast { it == event.route }
                    if (index >= 0) {
                        while (backStack.lastIndex > index) backStack.removeAt(backStack.lastIndex)
                    } else if (backStack.lastOrNull() != event.route) {
                        backStack.add(event.route)
                    }
                }
                is Navigator.NavigateBottomBar -> {
                    while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                    if (backStack.lastOrNull() != event.route) backStack.add(event.route)
                }
                Navigator.NavigateBack -> {
                    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) else onBack()
                }
                is Navigator.NavigatePopBackStack -> popStackTo(backStack, event.route, event.inclusive)
                Navigator.FinishActivity -> onFinish()
                is Navigator.NavigateBackWithResult -> {
                    navigator.emitResult(event.result)
                    onResult(event.result)
                    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) else onBack()
                }
                is Navigator.NavigateCustom -> {
                    @Suppress("UNCHECKED_CAST")
                    onNavigateCustom(event.route as Route)
                }
            }
        }
    }


    CompositionLocalProvider(LocalNiddleNavigator provides navigator) {
        NiddleNavHost(
            backStack = backStack,
            entryProvider = entryProvider,
            onBack = {
                if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) else onBack()
            },
            transitionSpec = transitionSpec,
            popTransitionSpec = popTransitionSpec,
            predictivePopTransitionSpec = predictivePopTransitionSpec,
        )
    }
}

/** Builds an entry provider using Navigation 3's official DSL. */
public fun niddleEntries(
    builder: EntryProviderScope<NavKey>.() -> Unit,
): NiddleEntryProvider = entryProvider(builder = builder)

/** Builds an entry provider and validates route ownership against [graph]. */
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

private fun popStackTo(
    backStack: NavBackStack<NavKey>,
    route: NavKey,
    inclusive: Boolean,
) {
    val index = backStack.indexOfLast { it == route }
    if (index < 0) return
    val targetSize = if (inclusive) index else index + 1
    if (targetSize < 1) return
    while (backStack.size > targetSize) backStack.removeAt(backStack.lastIndex)
}

