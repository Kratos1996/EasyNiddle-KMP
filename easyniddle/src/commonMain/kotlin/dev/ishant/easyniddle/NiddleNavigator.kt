package dev.ishant.easyniddle

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Command-based navigator compatible with the original EasyNiddle API.
 *
 * The navigator is deliberately independent from the UI and the actual stack.
 * Create one per navigation host and pass it to [NiddleNavHost]. It can also be
 * injected through your DI framework, provided the same instance is supplied
 * to that host.
 *
 * Events are buffered and delivered to the host's navigation event collector.
 */
public class NiddleNavigator {
    private val events = MutableSharedFlow<NiddleNavigationEvent>(extraBufferCapacity = 64)

    /** A read-only stream of navigation commands consumed by [NiddleNavHost]. */
    public val navEvents = events.asSharedFlow()

    /** Pushes [route] onto the current stack. */
    public fun navigate(route: NavKey) {
        events.tryEmit(NiddleNavigationEvent.Navigate(route))
    }

    /**
     * Pops to [popUpTo] and pushes [route].
     * @param inclusive whether the matched [popUpTo] entry is removed as well.
     */
    public fun navigate(route: NavKey, popUpTo: NavKey, inclusive: Boolean = false) {
        events.tryEmit(NiddleNavigationEvent.NavigateWithPopUp(route, popUpTo, inclusive))
    }

    /** Pops to an existing [route], or pushes it when it is not present. */
    public fun navigateSingleTop(route: NavKey) {
        events.tryEmit(NiddleNavigationEvent.NavigateSingleTop(route))
    }

    /**
     * Selects a bottom-navigation destination, retaining the root and avoiding
     * a duplicate top entry. For true independent per-tab history, use
     * [NiddleMultiStack] instead.
     */
    public fun navigateBottomBar(route: NavKey) {
        events.tryEmit(NiddleNavigationEvent.NavigateBottomBar(route))
    }

    /** Pops the current destination; the host's onBack callback handles the root. */
    public fun pop() {
        events.tryEmit(NiddleNavigationEvent.Back)
    }

    /** Pops to the last matching [route], optionally removing the match. */
    public fun popTo(route: NavKey, inclusive: Boolean = false) {
        events.tryEmit(NiddleNavigationEvent.PopTo(route, inclusive))
    }

    /** Emits the host-level finish request. The host invokes its [NiddleNavHost.onFinish]. */
    public fun finish() {
        events.tryEmit(NiddleNavigationEvent.Finish)
    }

    /** Sends [result] to the host-scoped result flow and pops the current destination. */
    public fun navigateBackWithResult(result: Any) {
        events.tryEmit(NiddleNavigationEvent.BackWithResult(result))
    }

    /** Emits an app-defined route command for [NiddleNavHost.onNavigateCustom]. */
    public fun navigateCustom(route: NavKey) {
        events.tryEmit(NiddleNavigationEvent.Custom(route))
    }

    internal suspend fun collectEvents(collector: suspend (NiddleNavigationEvent) -> Unit) {
        navEvents.collect(collector)
    }
}

/** The navigator available to all descendants of the simple host overload. */
public val LocalNiddleNavigator = staticCompositionLocalOf<NiddleNavigator> {
    error("No NiddleNavigator provided. Place this composable inside NiddleNavHost.")
}

/** Events internal to the navigator/host bridge. */
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

/**
 * Result emitted when a destination calls [NiddleNavigator.navigateBackWithResult].
 * The stream is scoped to this host instance and is not persisted.
 */
public class NiddleNavigationResults internal constructor(
    private val events: MutableSharedFlow<Any>,
) {
    /** Stream of results sent by destinations. */
    public val results = events.asSharedFlow()
}
