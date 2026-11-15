package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

/**
 * Immutable route ownership metadata. Navigation 3 still owns rendering,
 * transitions, and the actual back stack.
 */
public class NiddleGraph internal constructor(
    public val startDestination: NavKey,
    internal val destinationTypes: Set<KClass<out NavKey>>,
    public val nestedGraphs: List<NiddleGraph>,
) {
    public fun contains(route: NavKey): Boolean =
        route::class in destinationTypes || nestedGraphs.any { it.contains(route) }

    public val routeTypes: Set<KClass<out NavKey>>
        get() = buildSet {
            addAll(destinationTypes)
            nestedGraphs.forEach { addAll(it.routeTypes) }
        }

    /** Throws a readable error when the route is not registered in this graph tree. */
    public fun requireContains(route: NavKey) {
        require(contains(route)) {
            "Route ${route::class.simpleName} is not registered in this NiddleGraph."
        }
    }
}

/** DSL for declaring route ownership and nested feature boundaries. */
public class NiddleGraphBuilder internal constructor(initialStartDestination: NavKey?) {
    private var configuredStartDestination: NavKey? = initialStartDestination
    private val destinations = linkedSetOf<KClass<out NavKey>>()
    private val children = mutableListOf<NiddleGraph>()

    public var startDestination: NavKey
        get() = requireNotNull(configuredStartDestination) {
            "NiddleGraph.startDestination must be configured."
        }
        set(value) { configuredStartDestination = value }

    public fun destination(routeType: KClass<out NavKey>) {
        require(destinations.add(routeType)) {
            "Duplicate destination registration: ${routeType.simpleName}"
        }
    }

    /** Register a route without manually passing its KClass. */
    public inline fun <reified Route : NavKey> destination() {
        destination(Route::class)
    }

    public fun nestedGraph(
        startDestination: NavKey,
        builder: NiddleGraphBuilder.() -> Unit,
    ) {
        children += niddleGraph(startDestination, builder)
    }

    internal fun build(): NiddleGraph {
        val start = requireNotNull(configuredStartDestination) {
            "NiddleGraph.startDestination must be configured."
        }
        require(start::class in destinations) {
            "Start destination ${start::class.simpleName} must be registered in its own graph."
        }
        val graph = NiddleGraph(start, destinations.toSet(), children.toList())
        val registeredCount = destinations.size + children.sumOf { it.routeTypes.size }
        require(graph.routeTypes.size == registeredCount) {
            "A route type is registered more than once in this graph tree."
        }
        return graph
    }
}

/** Builds an immutable graph and validates start destinations and duplicate routes. */
public fun niddleGraph(
    startDestination: NavKey,
    builder: NiddleGraphBuilder.() -> Unit,
): NiddleGraph {
    val graphBuilder = NiddleGraphBuilder(startDestination)
    graphBuilder.builder()
    return graphBuilder.build()
}
