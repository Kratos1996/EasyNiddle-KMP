package dev.ishant.easyniddle

/**
 * Small multiplatform navigation logger.
 *
 * Logging is disabled by default. Set [enabled] in the app during development.
 * The library does not depend on Android Logcat or a platform-specific logger.
 */
public object NiddleLogger {
    /** Enables debug messages for navigation operations. Defaults to false. */
    public var enabled: Boolean = false

    /** Optional app-provided logging sink. */
    public var logger: ((String) -> Unit)? = null

    /** Emits [message] only when logging is enabled and a [logger] is installed. */
    public fun d(message: String) {
        if (enabled) logger?.invoke("[EasyNiddle] $message")
    }
}
