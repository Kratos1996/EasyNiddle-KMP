package dev.ishant.easyniddle

/** Deterministic stack policies independent of Compose and platform APIs. */
public object BackStackOperations {
    /** Remove the top item without removing the root. */
    public fun <T> pop(stack: MutableList<T>): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** Pop until the last matching target is topmost. */
    public fun <T> popTo(stack: MutableList<T>, target: T, inclusive: Boolean = false): Boolean {
        val index = stack.indexOfLast { it == target }
        if (index < 0) return false
        val newSize = if (inclusive) index else index + 1
        if (newSize < 1 || newSize >= stack.size) return false
        while (stack.size > newSize) stack.removeAt(stack.lastIndex)
        return true
    }

    /** Add destination only when it is not already the current top. */
    public fun <T> navigateSingleTop(stack: MutableList<T>, destination: T) {
        if (stack.lastOrNull() != destination) stack.add(destination)
    }
}
