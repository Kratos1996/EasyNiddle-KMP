package dev.ishant.easyniddle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable

@Serializable
private data class DeepLinkProfile(val userId: String, val source: String?) : NiddleRoute

@Serializable
private data object DeepLinkHome : NiddleRoute

class NiddleDeepLinkTest {
    @Test
    fun resolvesTypedRouteFromPathAndQueryParameters() {
        val registry = niddleDeepLinks {
            deepLink("https://example.com/users/{userId}") { match ->
                DeepLinkProfile(
                    userId = match.pathParameter("userId"),
                    source = match.queryParameter("source"),
                )
            }
        }

        assertEquals(
            DeepLinkProfile("user-42", "email invite"),
            registry.resolve("https://example.com/users/user-42?source=email+invite"),
        )
    }

    @Test
    fun decodesPercentEncodedPathAndQueryValues() {
        val registry = niddleDeepLinks {
            deepLink("myapp://profile/{name}") { match ->
                DeepLinkProfile(
                    userId = match.pathParameter("name"),
                    source = match.queryParameter("from"),
                )
            }
        }

        assertEquals(
            DeepLinkProfile("A B", "mobile app"),
            registry.resolve("myapp://profile/A%20B?from=mobile%20app"),
        )
    }

    @Test
    fun prefersMoreSpecificPattern() {
        val registry = niddleDeepLinks {
            deepLink("https://example.com/users/{userId}") {
                DeepLinkProfile(it.pathParameter("userId"), "generic")
            }
            deepLink("https://example.com/users/me") {
                DeepLinkProfile("current-user", "specific")
            }
        }

        assertEquals(
            DeepLinkProfile("current-user", "specific"),
            registry.resolve("https://example.com/users/me"),
        )
    }

    @Test
    fun returnsNullWhenNoPatternMatches() {
        val registry = niddleDeepLinks {
            deepLink("https://example.com/home") { DeepLinkHome }
        }

        assertNull(registry.resolve("https://other.com/home"))
        assertNull(registry.resolve("https://example.com/profile"))
    }

    @Test
    fun requireResolveExplainsMissingPattern() {
        val registry = niddleDeepLinks {
            deepLink("https://example.com/home") { DeepLinkHome }
        }

        val error = assertFailsWith<IllegalArgumentException> {
            registry.requireResolve("https://example.com/unknown")
        }
        assertTrue(error.message.orEmpty().contains("No Niddle deep-link pattern"))
    }

    @Test
    fun rejectsDuplicatePatterns() {
        assertFailsWith<IllegalArgumentException> {
            niddleDeepLinks {
                deepLink("https://example.com/home") { DeepLinkHome }
                deepLink("https://example.com/home") { DeepLinkHome }
            }
        }
    }

    @Test
    fun rejectsDuplicatePathParameterNames() {
        assertFailsWith<IllegalArgumentException> {
            niddleDeepLinks {
                deepLink("https://example.com/users/{id}/posts/{id}") { DeepLinkHome }
            }
        }
    }

    @Test
    fun rejectsAmbiguousPatternsWithSameSpecificity() {
        val registry = niddleDeepLinks {
            deepLink("https://example.com/{first}/profile") { DeepLinkHome }
            deepLink("https://example.com/users/{second}") { DeepLinkHome }
        }

        assertFailsWith<IllegalStateException> {
            registry.resolve("https://example.com/users/profile")
        }
    }
}
