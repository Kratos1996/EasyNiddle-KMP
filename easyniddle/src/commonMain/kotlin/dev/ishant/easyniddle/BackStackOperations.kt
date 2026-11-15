package dev.ishant.easyniddle

/**
 * Pure, deterministic back-stack policies that do not depend on Compose or a
 * platform API. These helpers mutate the list supplied by the caller.
 */
public object BackStackOperations {
    /**
     * Removes the top element while preserving the root element.
     *
     * @return true if an element was removed; false if the stack has zero or
     * one elements.
     */
    public fun <T> pop(stack: MutableList<T>): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /**
     * Removes entries until the last [target] match is topmost.
     *
     * If [inclusive] is true, the matching target is removed too. The root is
     * never removed; absent targets and operations that would empty the stack
     * return false without changing it.
     */
    public fun <T> popTo(stack: MutableList<T>, target: T, inclusive: Boolean = false): Boolean {
        val index = stack.indexOfLast { it == target }
        if (index < 0) return false
        val newSize = if (inclusive) index else index + 1
        if (newSize < 1 || newSize >= stack.size) return false
        while (stack.size > newSize) stack.removeAt(stack.lastIndex)
        return true
    }

    /** Adds [destination] only when it is not already the current top element. */
    public fun <T> navigateSingleTop(stack: MutableList<T>, destination: T) {
        if (stack.lastOrNull() != destination) stack.add(destination)
    }
}
