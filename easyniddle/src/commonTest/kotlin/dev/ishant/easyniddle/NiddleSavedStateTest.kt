package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
private data object RestoreHomeRoute : NiddleRoute

@Serializable
private data class RestoreProfileRoute(val userId: String, val tab: Int) : NiddleRoute

@Serializable
private data object RestoreSettingsRoute : NiddleRoute

class NiddleSavedStateTest {
    @Test
    fun polymorphicRouteStackCanBeSerializedAndRestored() {
        val module = niddleSavedStateConfiguration {
            route(RestoreHomeRoute.serializer())
            route(RestoreProfileRoute.serializer())
            route(RestoreSettingsRoute.serializer())
        }.serializersModule

        val json = Json { serializersModule = module }
        val stackSerializer = ListSerializer(PolymorphicSerializer(NavKey::class))
        val originalStack: List<NavKey> = listOf(
            RestoreHomeRoute,
            RestoreProfileRoute(userId = "user-42", tab = 3),
            RestoreSettingsRoute,
        )

        val encoded = json.encodeToString(stackSerializer, originalStack)
        val restored = json.decodeFromString(stackSerializer, encoded)

        assertEquals(originalStack, restored)
    }

    @Test
    fun restoresRouteArgumentsWithoutLosingTheirValues() {
        val module = niddleSavedStateConfiguration {
            route(RestoreHomeRoute.serializer())
            route(RestoreProfileRoute.serializer())
        }.serializersModule
        val json = Json { serializersModule = module }
        val serializer = PolymorphicSerializer(NavKey::class)
        val original: NavKey = RestoreProfileRoute("user-99", 7)

        val restored = json.decodeFromString<NavKey>(
            serializer,
            json.encodeToString(serializer, original),
        )

        assertEquals(original, restored)
    }

    @Test
    fun rejectsDuplicateRouteSerializerRegistration() {
        assertFailsWith<IllegalArgumentException> {
            niddleSavedStateConfiguration {
                route(RestoreHomeRoute.serializer())
                route(RestoreHomeRoute.serializer())
            }
        }
    }
}
