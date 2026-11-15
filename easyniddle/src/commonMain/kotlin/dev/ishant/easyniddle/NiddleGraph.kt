package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

/** Immutable route metadata; rendering remains delegated to Navigation 3. */
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
}

/** DSL for typed route ownership and nested graph boundaries. */
public class NiddleGraphBuilder internal constructor(initialStartDestination: NavKey?) {
    private var configuredStartDestination: NavKey? = initialStartDestination
    private val destinations = linkedSetOf<KClass<out NavKey>>()
    private val children = mutableListOf<NiddleGraph>()

    public var startDestination: NavKey
        get() = requireNotNull(configuredStartDestination) {
            "NiddleGraph.startDestination must be configured"
        }
        set(value) { configuredStartDestination = value }

    public fun destination(routeType: KClass<out NavKey>) {
        require(destinations.add(routeType)) {
            "Duplicate destination registration: " + routeType
        }
    }

    public fun nestedGraph(
        startDestination: NavKey,
        builder: NiddleGraphBuilder.() -> Unit,
    ) {
        children += niddleGraph(startDestination, builder)
    }

    internal fun build(): NiddleGraph {
        val start = requireNotNull(configuredStartDestination) {
            "NiddleGraph.startDestination must be configured"
        }
        require(start::class in destinations) {
            "Start destination must be registered in this graph: " + start::class
        }
        val graph = NiddleGraph(start, destinations.toSet(), children.toList())
        val expectedCount = destinations.size + children.sumOf { it.routeTypes.size }
        require(graph.routeTypes.size == expectedCount) {
            "A route type is registered more than once in this graph tree"
        }
        return graph
    }
}

/** Create an immutable graph with duplicate-route and start-destination validation. */
public fun niddleGraph(
    startDestination: NavKey,
    builder: NiddleGraphBuilder.() -> Unit,
): NiddleGraph {
    val graphBuilder = NiddleGraphBuilder(startDestination)
    graphBuilder.builder()
    return graphBuilder.build()
}
