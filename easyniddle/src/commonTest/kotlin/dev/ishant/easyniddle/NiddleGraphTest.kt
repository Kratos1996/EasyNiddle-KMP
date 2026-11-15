package dev.ishant.easyniddle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable

@Serializable
private data object GraphHome : NiddleRoute
@Serializable
private data class GraphProfile(val id: String) : NiddleRoute
@Serializable
private data object GraphSettings : NiddleRoute

class NiddleGraphTest {
    @Test
    fun registersTypedDestinationsAndFindsNestedRoutes() {
        val graph = niddleGraph(GraphHome) {
            destination<GraphHome>()
            destination<GraphProfile>()
            nestedGraph(GraphSettings) {
                destination<GraphSettings>()
            }
        }
        assertTrue(graph.contains(GraphProfile("42")))
        assertTrue(graph.contains(GraphSettings))
        assertEquals(3, graph.routeTypes.size)
    }

    @Test
    fun rejectsDuplicateDestinationInSameGraph() {
        assertFailsWith<IllegalArgumentException> {
            niddleGraph(GraphHome) {
                destination<GraphHome>()
                destination<GraphHome>()
            }
        }
    }

    @Test
    fun rejectsDuplicateDestinationAcrossNestedGraphs() {
        assertFailsWith<IllegalArgumentException> {
            niddleGraph(GraphHome) {
                destination<GraphHome>()
                nestedGraph(GraphProfile("child")) {
                    destination<GraphProfile>()
                }
                nestedGraph(GraphProfile("second")) {
                    destination<GraphProfile>()
                }
            }
        }
    }

    @Test
    fun rejectsUnregisteredStartDestination() {
        assertFailsWith<IllegalArgumentException> {
            niddleGraph(GraphHome) {
                destination<GraphProfile>()
            }
        }
    }

    @Test
    fun reportsUnknownRouteAsNotContained() {
        val graph = niddleGraph(GraphHome) {
            destination<GraphHome>()
        }
        assertFalse(graph.contains(GraphSettings))
    }

    @Test
    fun requireContainsExplainsUnknownRoute() {
        val graph = niddleGraph(GraphHome) {
            destination<GraphHome>()
        }
        val error = assertFailsWith<IllegalArgumentException> {
            graph.requireContains(GraphSettings)
        }
        assertTrue(error.message.orEmpty().contains("GraphSettings"))
    }
}
