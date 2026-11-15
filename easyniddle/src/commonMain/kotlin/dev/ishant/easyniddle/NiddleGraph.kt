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
    /** Returns whether [route]'s concrete type belongs to this graph tree. */
    public fun contains(route: NavKey): Boolean =
        route::class in destinationTypes || nestedGraphs.any { it.contains(route) }

    /** All route classes owned by this graph and every nested graph. */
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

/** DSL receiver for declaring route ownership and nested feature boundaries. */
public class NiddleGraphBuilder internal constructor(initialStartDestination: NavKey?) {
    private var configuredStartDestination: NavKey? = initialStartDestination
    private val destinations = linkedSetOf<KClass<out NavKey>>()
    private val children = mutableListOf<NiddleGraph>()

    /** Start route owned by this graph; it must be registered with [destination]. */
    public var startDestination: NavKey
        get() = requireNotNull(configuredStartDestination) {
            "NiddleGraph.startDestination must be configured."
        }
        set(value) { configuredStartDestination = value }

    /** Registers [routeType] in this graph; duplicate registrations are rejected. */
    public fun destination(routeType: KClass<out NavKey>) {
        require(destinations.add(routeType)) {
            "Duplicate destination registration: ${routeType.simpleName}"
        }
    }

    /** Register a route without manually passing its KClass. */
    public inline fun <reified Route : NavKey> destination() {
        destination(Route::class)
    }

    /** Adds a nested feature graph with its own [startDestination] and route declarations. */
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

public fun niddleGraph(
    startDestination: NavKey,
    builder: NiddleGraphBuilder.() -> Unit,
): NiddleGraph {
    val graphBuilder = NiddleGraphBuilder(startDestination)
    graphBuilder.builder()
    return graphBuilder.build()
}
