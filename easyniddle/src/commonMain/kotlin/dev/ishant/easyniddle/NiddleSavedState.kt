package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Builder for the polymorphic serializers required by Navigation 3's
 * multiplatform NavBackStack persistence.
 */
public class NiddleSerializersModuleBuilder internal constructor() {
    private val routeSerializers = linkedMapOf<String, KSerializer<out NavKey>>()

    /**
     * Registers one concrete serializable route type.
     *
     * Register every route type that can enter this host's back stack.
     */
    public fun <Route : NavKey> route(serializer: KSerializer<Route>) {
        val serialName = serializer.descriptor.serialName
        require(serialName !in routeSerializers) {
            "Duplicate Niddle route serializer: '$serialName'."
        }
        routeSerializers[serialName] = serializer
    }

    internal fun build(): SerializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            routeSerializers.values.forEach { serializer ->
                subclass(serializer = serializer)
            }
        }
    }
}

/**
 * Creates a [SavedStateConfiguration] with explicit polymorphic serializers for
 * all app-owned [NavKey] route types.
 *
 * Pass the result to [rememberNiddleBackStack]. Navigation 3 uses this
 * configuration to save and restore the back stack across supported lifecycle
 * and process recreation events.
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
