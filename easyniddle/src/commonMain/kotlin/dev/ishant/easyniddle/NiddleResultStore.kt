package dev.ishant.easyniddle

/**
 * A typed key for a one-shot result shared between destinations.
 *
 * Keep keys as stable properties (for example, in an object) and reuse the same
 * key instance when writing and consuming a result.
 *
 * Keys use identity rather than their [name] for equality, so two independently
 * created keys cannot accidentally receive each other's values.
 */
public class NiddleResultKey<T : Any>(
    public val name: String,
) {
    init {
        require(name.isNotBlank()) { "NiddleResultKey name must not be blank." }
    }

    override fun toString(): String = "NiddleResultKey($name)"
}

/**
 * An in-memory, one-shot result store.
 *
 * Create one store per navigation host (or other navigation scope), pass it to
 * the destinations that need to exchange results, and discard it with that
 * scope. Results are removed as soon as they are consumed.
 *
 * This store is intentionally not persisted or process-restorable. It is meant
 * for short-lived destination-to-destination communication, not durable app data.
 * Access it from the UI/navigation thread; it does not provide synchronization.
 */
public class NiddleResultStore {
    private val values: MutableMap<NiddleResultKey<*>, Any> = mutableMapOf()

    /** Stores or replaces the pending value associated with [key]. */
    public fun <T : Any> set(key: NiddleResultKey<T>, value: T) {
        values[key] = value
    }

    /**
     * Returns and removes the pending value for [key].
     *
     * Returns null when no value is waiting. A result can be consumed only once.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> consume(key: NiddleResultKey<T>): T? {
        return values.remove(key) as T?
    }

    /** Returns whether [key] currently has a pending result. */
    public fun hasResult(key: NiddleResultKey<*>): Boolean = values.containsKey(key)

    /** Removes a pending result without consuming it. */
    public fun clear(key: NiddleResultKey<*>) {
        values.remove(key)
    }

    /** Removes every pending result in this navigation scope. */
    public fun clearAll() {
        values.clear()
    }
}
