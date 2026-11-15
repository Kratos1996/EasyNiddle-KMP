package dev.ishant.easyniddle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * State holder for independent bottom-navigation back stacks.
 *
 * Each tab keeps its own history. Switching tabs never clears another tab's
 * stack. The class is snapshot-aware, so reads from Compose UI automatically
 * recompose when the selected tab or a stack changes.
 *
 * Create one instance per navigation host; do not share it globally.
 */
public class NiddleMultiStack<Tab : Any, Route : Any>(
    roots: Map<Tab, Route>,
    initialTab: Tab,
) {
    private val stacks = mutableStateMapOf<Tab, androidx.compose.runtime.snapshots.SnapshotStateList<Route>>()

    /** Selected tab; reads from Compose observe snapshot changes. */
    public var selectedTab: Tab by mutableStateOf(initialTab)
        private set

    init {
        require(roots.isNotEmpty()) { "At least one tab root is required." }
        require(initialTab in roots) { "Initial tab must exist in roots." }
        roots.forEach { (tab, root) -> stacks[tab] = mutableStateListOf(root) }
    }

    /** Tabs in their configured insertion order. */
    public val tabs: List<Tab>
        get() = stacks.keys.toList()

    /** Current tab's stack, returned as a read-only snapshot. */
    public val currentStack: List<Route>
        get() = stackFor(selectedTab)

    /** Returns a read-only snapshot of [tab]'s stack. */
    public fun stackFor(tab: Tab): List<Route> =
        requireNotNull(stacks[tab]) { "Unknown tab: $tab" }.toList()

    /**
     * Selects a tab. When the already-selected tab is tapped again,
     * [popToRootOnReselect] optionally resets only that tab's stack.
     */
    public fun selectTab(tab: Tab, popToRootOnReselect: Boolean = false) {
        require(tab in stacks) { "Unknown tab: $tab" }
        if (tab == selectedTab) {
            if (popToRootOnReselect) popToRoot(tab)
            return
        }
        selectedTab = tab
    }

    /** Pushes a destination on the active tab's stack. */
    /** Pushes [route] onto the selected tab's history. */
    public fun navigate(route: Route) {
        stacks.getValue(selectedTab).add(route)
    }

    /** Pushes only when [route] is not already the active top destination. */
    public fun navigateSingleTop(route: Route) {
        val stack = stacks.getValue(selectedTab)
        if (stack.lastOrNull() != route) stack.add(route)
    }

    /** Pops the active stack while preserving its root. */
    /** Pops the selected tab's top route while preserving its root. */
    public fun back(): Boolean {
        val stack = stacks.getValue(selectedTab)
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** Pops to the last matching destination on the active stack. */
    /**
     * Pops the selected tab to the last [route] match. With [inclusive],
     * removes the match too; the root is never removed. Returns false if no
     * valid change can be made.
     */
    public fun popTo(route: Route, inclusive: Boolean = false): Boolean {
        val stack = stacks.getValue(selectedTab)
        val index = stack.indexOfLast { it == route }
        if (index < 0) return false
        val targetSize = if (inclusive) index else index + 1
        if (targetSize < 1 || targetSize >= stack.size) return false
        while (stack.size > targetSize) stack.removeAt(stack.lastIndex)
        return true
    }

    /** Resets one tab to its root. */
    /** Resets [tab] to its configured root; defaults to the selected tab. */
    public fun popToRoot(tab: Tab = selectedTab) {
        val stack = requireNotNull(stacks[tab]) { "Unknown tab: $tab" }
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
}
