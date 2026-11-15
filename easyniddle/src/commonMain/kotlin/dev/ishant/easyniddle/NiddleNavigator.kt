package dev.ishant.easyniddle

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect

/**
 * Command-based navigator compatible with the original EasyNiddle API.
 *
 * Create one navigator per navigation host. It can be injected with Hilt,
 * Koin, or another DI framework, as long as the same instance is passed to
 * that host. It does not own the back stack; the host applies each command.
 */
public class NiddleNavigator {
    private val events = MutableSharedFlow<Navigator>(extraBufferCapacity = 64)
    private val resultEvents = MutableSharedFlow<Any>(extraBufferCapacity = 16)

    /** Public command stream, useful for integrations and tests. */
    public val navEvents = events.asSharedFlow()

    /** One-shot results emitted by [navigateBackWithResult]. */
    public val navigationResult = resultEvents.asSharedFlow()

    /** Pushes [route] onto the current host's stack. */
    public fun navigate(route: NiddleRoute) {
        events.tryEmit(Navigator.NavigateRoute(route))
    }

    /** Pops to [popUpTo] and then pushes [route]. */
    public fun navigate(route: NiddleRoute, popUpTo: NiddleRoute, inclusive: Boolean = false) {
        events.tryEmit(Navigator.NavigateWithPopUp(route, popUpTo, inclusive))
    }

    /** Returns to an existing [route], removing entries above it, or pushes it if absent. */
    public fun navigateSingleTop(route: NiddleRoute) {
        events.tryEmit(Navigator.NavigateToBackstackScreenAsSingleTop(route))
    }

    /** Selects a bottom-bar destination using root-retaining single-top behavior. */
    public fun navigateBottomBar(route: NiddleRoute) {
        events.tryEmit(Navigator.NavigateBottomBar(route))
    }

    /** Pops the current destination; the host's onBack callback handles a root request. */
    public fun pop() {
        events.tryEmit(Navigator.NavigateBack)
    }

    /** Pops to the last [route] match, optionally removing the match. */
    public fun popTo(route: NiddleRoute, inclusive: Boolean = false) {
        events.tryEmit(Navigator.NavigatePopBackStack(route, inclusive))
    }

    /** Requests the host's platform-specific finish action. */
    public fun finish() {
        events.tryEmit(Navigator.FinishActivity)
    }

    /** Publishes [result] to [navigationResult] and pops the current destination. */
    public fun navigateBackWithResult(result: Any) {
        events.tryEmit(Navigator.NavigateBackWithResult(result))
    }

    /** Sends [route] to the host's app-specific custom-navigation callback. */
    public fun navigateCustom(route: NiddleRoute) {
        events.tryEmit(Navigator.NavigateCustom(route))
    }

    /** Accepts command objects used by the original Android EasyNiddle API. */
    public fun navigate(command: Navigator) {
        events.tryEmit(command)
    }

    internal suspend fun collectEvents(collector: suspend (Navigator) -> Unit) {
        navEvents.collect(collector)
    }

    internal fun emitResult(result: Any) {
        resultEvents.tryEmit(result)
    }
}

/** Access the host-scoped navigator from any descendant composable. */
public val LocalNiddleNavigator = staticCompositionLocalOf<NiddleNavigator> {
    error("No NiddleNavigator provided. Place this composable inside NiddleNavHost.")
}

/** Navigation command names retained for source-level migration from the Android API. */
public sealed interface Navigator {
    public data class NavigateRoute(val route: NiddleRoute) : Navigator
    public data class NavigatePopBackStack(
        val route: NiddleRoute,
        val inclusive: Boolean,
        val saveState: Boolean = false,
    ) : Navigator
    public data class NavigateBottomBar(
        val route: NiddleRoute,
        val saveState: Boolean = true,
        val launchSingleTop: Boolean = true,
        val restoreState: Boolean = true,
    ) : Navigator
    public data class NavigateWithPopUp(
        val route: NiddleRoute,
        val popUpToRoute: NiddleRoute,
        val inclusive: Boolean = false,
    ) : Navigator
    public data class NavigateToBackstackScreenAsSingleTop(val route: NiddleRoute) : Navigator
    public data object FinishActivity : Navigator
    public data object NavigateBack : Navigator
    public data class NavigateBackWithResult(val result: Any) : Navigator
    public data class NavigateCustom(val route: NiddleRoute) : Navigator
}
