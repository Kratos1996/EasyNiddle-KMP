# Migration guide

EasyNiddle is a convenience layer over Navigation 3, not a drop-in replacement for Navigation Compose's `NavController` or `NavHost`. Migrate one navigation host at a time and keep the app's route data and destination UI.

## 1. Convert destinations into route keys

Replace string route names and loosely typed argument bundles with small immutable route types implementing `NiddleRoute`.

Before (conceptual Navigation Compose route):

```kotlin
// "profile/{userId}" with a string argument
```

After:

```kotlin
@Serializable
data object HomeRoute : NiddleRoute

@Serializable
data class ProfileRoute(val userId: String) : NiddleRoute
```

Keep route arguments serializable and stable. Prefer IDs over passing full domain objects, ViewModels, Compose state, or platform references.

## 2. Register serializers for restoration

Create one configuration for the route types that may appear in the host's stack.

```kotlin
val savedStateConfig = niddleSavedStateConfiguration {
    route(HomeRoute.serializer())
    route(ProfileRoute.serializer())
}
```

Every concrete route type that can be pushed must be registered. This is required for polymorphic Navigation 3 back-stack persistence.

## 3. Replace NavController ownership with a caller-owned back stack

Create the Navigation 3 stack in the Compose host:

```kotlin
val backStack = rememberNiddleBackStack(
    savedStateConfiguration = savedStateConfig,
    startDestination = HomeRoute,
)
val navigator = NiddleController(backStack)
```

Use `navigator.navigate(ProfileRoute(userId))`, `navigator.back()`, or mutate the Navigation 3 stack directly when you need lower-level control. There is no global controller: each host owns its stack.

## 4. Move NavHost destinations into an entry provider

Register destination UI with Navigation 3's entry DSL, optionally guarded by a graph.

```kotlin
val graph = niddleGraph(startDestination = HomeRoute) {
    destination<HomeRoute>()
    destination<ProfileRoute>()
}

val entries = niddleEntries(graph) {
    entry<HomeRoute> { HomeScreen() }
    entry<ProfileRoute> { route ->
        ProfileScreen(userId = route.userId)
    }
}

NiddleNavHost(backStack = backStack, entryProvider = entries)
```

The graph-aware overload checks route ownership. The plain `niddleEntries { ... }` overload is available when you manage registration separately.

## 5. Move platform deep-link handling to the app boundary

Keep Android App Links, iOS Universal Links, and browser URL handlers in the host application. Pass the received URL to `niddleDeepLinks { ... }` and push the returned typed route. EasyNiddle resolves URL patterns; it does not register operating-system URL handlers.

## 6. Validate behavior before removing the old host

Migrate incrementally and verify:
- Back behavior at the root and on nested destinations.
- Route argument serialization and process restoration on each target you ship.
- Deep links for known, unknown, and malformed URLs.
- Predictive back and custom transitions on supported platforms.
- Bottom-tab history if your app previously used multiple back stacks.

## Compatibility notes

- EasyNiddle APIs wrap Navigation 3 types; Navigation 3 remains responsible for rendering and back-stack state.
- `NiddleResultStore` is an in-memory one-shot hand-off, not a replacement for saved state or persistent storage.
- `NiddleMultiStack` provides independent snapshot-aware tab histories; do not assume it is automatically persisted by `rememberNiddleBackStack`.
- Host-level process recreation tests on Android and iOS remain a separate integration-test requirement.
