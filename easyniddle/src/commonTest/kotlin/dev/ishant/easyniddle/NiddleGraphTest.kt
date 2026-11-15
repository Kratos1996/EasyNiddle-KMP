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
            destination(GraphHome::class)
            destination(GraphProfile::class)
            nestedGraph(GraphSettings) {
                destination(GraphSettings::class)
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
                destination(GraphHome::class)
                destination(GraphHome::class)
            }
        }
    }

    @Test
    fun rejectsDuplicateDestinationAcrossNestedGraphs() {
        assertFailsWith<IllegalArgumentException> {
            niddleGraph(GraphHome) {
                destination(GraphHome::class)
                nestedGraph(GraphProfile("child")) {
                    destination(GraphProfile::class)
                }
                nestedGraph(GraphProfile("second")) {
                    destination(GraphProfile::class)
                }
            }
        }
    }

    @Test
    fun rejectsUnregisteredStartDestination() {
        assertFailsWith<IllegalArgumentException> {
            niddleGraph(GraphHome) {
                destination(GraphProfile::class)
            }
        }
    }

    @Test
    fun reportsUnknownRouteAsNotContained() {
        val graph = niddleGraph(GraphHome) {
            destination(GraphHome::class)
        }
        assertFalse(graph.contains(GraphSettings))
    }
}
