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
 * Builder for the polymorphic serializers required by Navigation 3's
 * multiplatform NavBackStack persistence.
 */
public class NiddleSerializersModuleBuilder internal constructor() {
    private val registeredNames = linkedSetOf<String>()
    private val registrations =
        mutableListOf<PolymorphicModuleBuilder<NavKey>.() -> Unit>()

    /**
     * Registers one concrete serializable route type.
     *
     * Register every route type that can enter this host's back stack.
     */
    public inline fun <reified Route : NavKey> route(serializer: KSerializer<Route>) {
        route(Route::class, serializer)
    }

    /** Registers a route serializer when its route class is only available as a value. */
    /** Registers [serializer] for [routeClass] in the polymorphic NavKey module. */
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
 * Creates a [SavedStateConfiguration] with explicit polymorphic serializers for
 * all app-owned [NavKey] route types.
 *
 * Pass the result to [rememberNiddleBackStack]. Register every concrete route
 * type that may enter the back stack. Actual host lifecycle restoration should
 * also be verified in app integration tests on each shipped platform.
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
