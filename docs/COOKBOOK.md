# Cookbook

Practical recipes for common navigation patterns. The snippets assume the route types are serializable and that the examples' UI functions are provided by your app.

## Bottom navigation with independent histories

Use one `NiddleMultiStack` per bottom-navigation host. Each tab retains its own stack when another tab is selected.

```kotlin
enum class AppTab { Home, Search, Profile }

sealed interface AppRoute
data object HomeRoute : AppRoute
data object SearchRoute : AppRoute
data object ProfileRoute : AppRoute
data class SearchDetailRoute(val query: String) : AppRoute

val tabs = NiddleMultiStack(
    roots = mapOf(
        AppTab.Home to HomeRoute,
        AppTab.Search to SearchRoute,
        AppTab.Profile to ProfileRoute,
    ),
    initialTab = AppTab.Home,
)

// From UI callbacks:
tabs.selectTab(AppTab.Search)
tabs.navigate(SearchDetailRoute("Compose"))
tabs.back() // Pops Search only; Home and Profile are unchanged.
tabs.selectTab(AppTab.Search, popToRootOnReselect = true)
```

Read `selectedTab`, `tabs`, and `currentStack` from Compose UI so snapshot state changes trigger recomposition. Keep the stack holder scoped to its host; it is not a process-persistent replacement for Navigation 3's saved back stack.

## Nested feature graphs

Let each feature own its start destination and route types. The root graph can include nested graphs while still validating the complete route tree.

```kotlin
val appGraph = niddleGraph(startDestination = HomeRoute) {
    destination<HomeRoute>()
    nestedGraph(startDestination = AccountRoute) {
        destination<AccountRoute>()
        destination<AccountDetailsRoute>()
    }
}
```

Register entries with `niddleEntries(appGraph) { ... }`. An entry request for a route type not registered in the graph fails immediately with a useful error.

## Deep links with optional query values

Register URL patterns in common code and map captured values to your serializable route.

```kotlin
val links = niddleDeepLinks {
    deepLink("https://example.com/products/{productId}") { match ->
        ProductRoute(
            id = match.pathParameter("productId"),
            campaign = match.queryParameter("campaign"),
        )
    }
}

val destination = links.resolve(receivedUrl)
if (destination != null) {
    backStack.add(destination)
}
```

The app still needs platform URL registration and must pass the received URL to the registry. Path placeholders occupy a complete path segment. Query values are decoded, and the last occurrence wins when a key appears more than once.

## One-shot results between destinations

Use a stable key instance shared by the sender and receiver. Consume the value when the previous destination becomes active again.

```kotlin
object ResultKeys {
    val selectedAddress = NiddleResultKey<String>("selected-address")
}

val results = NiddleResultStore()

// In the picker destination:
results.set(ResultKeys.selectedAddress, addressId)
navigator.back()

// In the destination that opened the picker:
val addressId = results.consume(ResultKeys.selectedAddress)
```

The store is in-memory, not thread-safe, and not restored after process death. Use a database or saved state for durable data.

## Custom transitions

The host accepts Compose Animation transition lambdas. Keep back-stack mutation in the navigation action; transition lambdas should only describe visuals.

```kotlin
NiddleNavHost(
    backStack = backStack,
    entryProvider = entries,
    transitionSpec = niddleSlideInTransition,
    popTransitionSpec = niddleSlidePopTransition,
    predictivePopTransitionSpec = niddlePredictiveFadeTransition,
)
```

## Single-top and replace behavior

```kotlin
val navigator = NiddleController(backStack)

navigator.navigateSingleTop(HomeRoute) // Adds only if HomeRoute isn't already on top.
navigator.replace(LoginRoute)           // Replaces the current top entry.
navigator.popTo(HomeRoute)              // Pops back to the last HomeRoute.
```

`navigateSingleTop` only checks the current top; it does not search the entire history. `popTo` preserves the root and returns false when the requested operation cannot be performed.
