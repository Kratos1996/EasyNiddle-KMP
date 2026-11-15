package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavBackStack

/**
 * Imperative convenience facade over one Navigation 3 back stack.
 *
 * Create one controller for the host whose [backStack] it wraps. The controller
 * does not own or copy the stack, so changes are immediately reflected by the
 * Navigation 3 host. Prefer keeping it scoped to the screen or navigation
 * feature rather than storing it in a global singleton.
 *
 * @param backStack app/host-owned Navigation 3 stack to mutate.
 */
public class NiddleController(private val backStack: NavBackStack<NavKey>) {
    /** Route currently at the top of the stack, or null when it is empty. */
    public val current: NavKey? get() = backStack.lastOrNull()

    /** Number of route entries currently in the stack. */
    public val size: Int get() = backStack.size

    /** Pushes [route] onto the current stack. */
    public fun navigate(route: NavKey) { backStack.add(route) }

    /**
     * Pushes [route] unless it is already the current top entry.
     *
     * This only prevents an adjacent duplicate; it does not search or remove
     * earlier occurrences elsewhere in the stack.
     */
    public fun navigateSingleTop(route: NavKey) {
        if (backStack.lastOrNull() != route) backStack.add(route)
    }

    /**
     * Removes the current entry while preserving the root entry.
     *
     * @return true when an entry was removed, or false when the stack has
     * fewer than two entries.
     */
    public fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    /**
     * Pops entries until the last matching [route] becomes the top entry.
     *
     * With [inclusive] true, the matching entry is removed as well. The root
     * cannot be removed, and the method returns false when the route is absent
     * or the requested operation would leave the stack empty.
     *
     * @return true when the stack changed.
     */
    public fun popTo(route: NavKey, inclusive: Boolean = false): Boolean {
        val index = backStack.indexOfLast { it == route }
        if (index < 0) return false
        val targetSize = if (inclusive) index else index + 1
        if (targetSize < 1 || targetSize >= backStack.size) return false
        while (backStack.size > targetSize) backStack.removeAt(backStack.lastIndex)
        return true
    }

    /** Replaces the current top entry, or inserts [route] when the stack is empty. */
    public fun replace(route: NavKey) {
        if (backStack.isEmpty()) backStack.add(route) else backStack[backStack.lastIndex] = route
    }
}
