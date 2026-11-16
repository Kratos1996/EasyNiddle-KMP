package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
private sealed interface CompatibilityAppRoute : SerializableRoute {
    @Serializable
    data object Home : CompatibilityAppRoute

    @Serializable
    data class Profile(val id: String) : CompatibilityAppRoute
}

class NiddleCompatibilityTest {
    @Test
    fun sealedLegacyRouteMarkerSerializesThroughOneRegisteredSerializer() {
        val module = niddleSavedStateConfiguration {
            route(CompatibilityAppRoute.Home.serializer())
            route(CompatibilityAppRoute.Profile.serializer())
        }.serializersModule

        val json = Json { serializersModule = module }
        val serializer = ListSerializer(PolymorphicSerializer(NavKey::class))
        val routes: List<NavKey> = listOf(
            CompatibilityAppRoute.Home,
            CompatibilityAppRoute.Profile("user-42"),
        )

        val restored = json.decodeFromString<List<NavKey>>(
            serializer,
            json.encodeToString(serializer, routes),
        )

        assertEquals(routes, restored)
    }
}
