package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.reflect.KClass

/**
 * DSL receiver for registering serializable route families with Navigation 3.
 *
 * A sealed route interface/class can be registered once with its generated
 * sealed serializer. For unrelated route classes, register each serializer.
 */
public class NiddleSerializersModuleBuilder internal constructor() {
    private val registeredNames = linkedSetOf<String>()
    private val registrations =
        mutableListOf<PolymorphicModuleBuilder<NavKey>.() -> Unit>()

    /** Registers [serializer] as a polymorphic Navigation 3 route family or concrete route. */
    public inline fun <reified Route : NavKey> route(serializer: KSerializer<Route>) {
        route(Route::class, serializer)
    }

    /** Registers [serializer] under [routeClass] in the polymorphic NavKey module. */
    public fun <Route : NavKey> route(
        routeClass: KClass<Route>,
        serializer: KSerializer<Route>,
    ) {
        val serialName = serializer.descriptor.serialName
        require(registeredNames.add(serialName)) {
            "Duplicate Niddle route serializer: '$serialName'."
        }
        registrations += {
            subclass(routeClass, serializer)
        }
    }

    internal fun build(): SerializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            registrations.forEach { register -> register() }
        }
    }
}

/**
 * Creates Navigation 3's save-state configuration for all registered route types.
 *
 * For a sealed route family, register the generated serializer once:
 *
 * ```kotlin
 * val config = niddleSavedStateConfiguration {
 *     route(AppRoute.serializer())
 * }
 * ```
 */
public fun niddleSavedStateConfiguration(
    builder: NiddleSerializersModuleBuilder.() -> Unit,
): SavedStateConfiguration {
    val serializersBuilder = NiddleSerializersModuleBuilder()
    serializersBuilder.builder()
    return SavedStateConfiguration {
        serializersModule = serializersBuilder.build()
    }
}
