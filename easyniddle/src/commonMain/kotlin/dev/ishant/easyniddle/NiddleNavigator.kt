package dev.ishant.easyniddle

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Command-based navigator compatible with the original EasyNiddle API.
 *
 * Create one navigator per navigation host. It can be injected with Hilt,
 * Koin, or another DI framework, as long as the same instance is passed to
 * that host. It does not own the back stack; the host applies each command.
 */
public class NiddleNavigator {
    private val events = MutableSharedFlow<NiddleNavigationEvent>(extraBufferCapacity = 64)
    private val resultEvents = MutableSharedFlow<Any>(extraBufferCapacity = 16)

    /** Navigation commands consumed by [NiddleNavHost]. */
    public val navEvents = events.asSharedFlow()

    /** One-shot results emitted by [navigateBackWithResult]. */
    public val navigationResult = resultEvents.asSharedFlow()

    /** Pushes [route] onto the current host's stack. */
    public fun navigate(route: NavKey) {
        events.tryEmit(NiddleNavigationEvent.Navigate(route))
    }

    /** Pops to [popUpTo] and then pushes [route]. */
    public fun navigate(route: NavKey, popUpTo: NavKey, inclusive: Boolean = false) {
        events.tryEmit(NiddleNavigationEvent.NavigateWithPopUp(route, popUpTo, inclusive))
    }

    /** Returns to an existing [route], removing entries above it, or pushes it if absent. */
    public fun navigateSingleTop(route: NavKey) {
        events.tryEmit(NiddleNavigationEvent.NavigateSingleTop(route))
    }

    /** Selects a bottom-bar destination using root-retaining single-top behavior. */
    public fun navigateBottomBar(route: NavKey) {
        events.tryEmit(NiddleNavigationEvent.NavigateBottomBar(route))
    }

    /** Pops the current destination; the host's onBack callback handles a root request. */
    public fun pop() {
        events.tryEmit(NiddleNavigationEvent.Back)
    }

    /** Pops to the last [route] match, optionally removing the match. */
    public fun popTo(route: NavKey, inclusive: Boolean = false) {
        events.tryEmit(NiddleNavigationEvent.PopTo(route, inclusive))
    }

    /** Requests the host's platform-specific finish action. */
    public fun finish() {
        events.tryEmit(NiddleNavigationEvent.Finish)
    }

    /** Publishes [result] to [navigationResult] and pops the current destination. */
    public fun navigateBackWithResult(result: Any) {
        events.tryEmit(NiddleNavigationEvent.BackWithResult(result))
    }

    /** Sends [route] to the host's app-specific custom-navigation callback. */
    public fun navigateCustom(route: NavKey) {
        events.tryEmit(NiddleNavigationEvent.Custom(route))
    }

    /** Accepts command objects used by the original Android EasyNiddle API. */
    public fun navigate(command: Navigator) {
        when (command) {
            is Navigator.NavigateRoute -> navigate(command.route)
            is Navigator.NavigatePopBackStack -> popTo(command.route, command.inclusive)
            is Navigator.NavigateBottomBar -> navigateBottomBar(command.route)
            is Navigator.NavigateWithPopUp -> navigate(command.route, command.popUpToRoute, command.inclusive)
            is Navigator.NavigateToBackstackScreenAsSingleTop -> navigateSingleTop(command.route)
            Navigator.FinishActivity -> finish()
            Navigator.NavigateBack -> pop()
            is Navigator.NavigateBackWithResult -> navigateBackWithResult(command.result)
            is Navigator.NavigateCustom -> navigateCustom(command.route)
        }
    }

    internal suspend fun collectEvents(collector: suspend (NiddleNavigationEvent) -> Unit) {
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

internal sealed interface NiddleNavigationEvent {
    data class Navigate(val route: NavKey) : NiddleNavigationEvent
    data class NavigateWithPopUp(val route: NavKey, val popUpTo: NavKey, val inclusive: Boolean) : NiddleNavigationEvent
    data class NavigateSingleTop(val route: NavKey) : NiddleNavigationEvent
    data class NavigateBottomBar(val route: NavKey) : NiddleNavigationEvent
    data object Back : NiddleNavigationEvent
    data class PopTo(val route: NavKey, val inclusive: Boolean) : NiddleNavigationEvent
    data object Finish : NiddleNavigationEvent
    data class BackWithResult(val result: Any) : NiddleNavigationEvent
    data class Custom(val route: NavKey) : NiddleNavigationEvent
}
