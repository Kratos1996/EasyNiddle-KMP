package dev.ishant.easyniddle

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavBackStack

/** Per-host imperative convenience facade over a Navigation 3 back stack. */
public class NiddleController(private val backStack: NavBackStack<NavKey>) {
    public val current: NavKey? get() = backStack.lastOrNull()
    public val size: Int get() = backStack.size

    public fun navigate(route: NavKey) { backStack.add(route) }

    public fun navigateSingleTop(route: NavKey) {
        if (backStack.lastOrNull() != route) backStack.add(route)
    }

    public fun back(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }

    public fun popTo(route: NavKey, inclusive: Boolean = false): Boolean {
        val index = backStack.indexOfLast { it == route }
        if (index < 0) return false
        val targetSize = if (inclusive) index else index + 1
        if (targetSize < 1 || targetSize >= backStack.size) return false
        while (backStack.size > targetSize) backStack.removeAt(backStack.lastIndex)
        return true
    }

    public fun replace(route: NavKey) {
        if (backStack.isEmpty()) backStack.add(route) else backStack[backStack.lastIndex] = route
    }
}
