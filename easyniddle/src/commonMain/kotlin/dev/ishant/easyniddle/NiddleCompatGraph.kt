package dev.ishant.easyniddle

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

/**
 * Builder for the original EasyNiddle `screen<Route> { }` API.
 *
 * This builder is intentionally minimal: graph ownership remains optional for
 * this compatibility facade, while route-to-entry mapping is type checked.
 */
public class NiddleNavGraphBuilder<Route : NavKey> {
    private val providers = linkedMapOf<KClass<out Route>, (Route) -> NavEntry<Route>>()

    /**
     * Registers the Compose content for one route type.
     * The route instance is passed to [content], including all serializable args.
     */
    public inline fun <reified Destination : Route> screen(
        noinline content: @Composable (Destination) -> Unit,
    ) {
        register(Destination::class) { route ->
            NavEntry(route) { content(route) }
        }
    }

    /**
     * Registers a route type using an explicit [routeType]. Normally prefer
     * [screen], which infers the type from its generic parameter.
     */
    public fun <Destination : Route> register(
        routeType: KClass<out Destination>,
        provider: (Destination) -> NavEntry<Destination>,
    ) {
        require(routeType !in providers) {
            "A screen is already registered for ${routeType.simpleName}."
        }
        providers[routeType] = { route ->
            @Suppress("UNCHECKED_CAST")
            provider(route as Destination)
        }
    }

    internal fun build(): (Route) -> NavEntry<Route> = { route ->
        val provider = providers[route::class]
            ?: error(
                "No screen defined for route: ${route::class.simpleName}. " +
                    "Register it with screen<${route::class.simpleName}> { } in NiddleNavHost."
            )
        provider(route)
    }
}

/** Builds the familiar EasyNiddle route-to-screen DSL. */
public fun <Route : NavKey> niddleScreenGraph(
    builder: NiddleNavGraphBuilder<Route>.() -> Unit,
): (Route) -> NavEntry<Route> =
    NiddleNavGraphBuilder<Route>().apply(builder).build()
