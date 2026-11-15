# Architecture

EasyNiddle is a human-friendly Kotlin Multiplatform API built on official Navigation 3 primitives. It removes repetitive setup while keeping the underlying navigation model visible. It is not a second navigation engine.

## The mental model

Think of navigation as four small pieces:

1. Routes describe where the user can go. They are serializable data types.
2. Graph describes which routes belong to a feature and where that feature starts.
3. Back stack describes the user's current navigation history.
4. Entries describe which UI to show for each route.

EasyNiddle gives each piece a clear API. Navigation 3 remains responsible for rendering, transitions, saveable entry state, and back-stack behavior.

## Main building blocks

| API | Responsibility |
| --- | --- |
| NiddleRoute | Marker for app-owned route keys |
| niddleGraph { } | Declares route ownership and nested feature boundaries |
| niddleEntries(graph) { } | Registers UI using Navigation 3's official entry DSL and validates route ownership |
| rememberNiddleBackStack() | Creates Navigation 3's saveable back stack |
| niddleSavedStateConfiguration { } | Registers polymorphic route serializers for multiplatform state restoration |
| NiddleNavHost() | Displays the current stack through Navigation 3 |
| NiddleController | Readable push, back, single-top, replace, and pop-to operations |
| NiddleMultiStack | Independent histories for bottom-navigation tabs |
| NiddleResultKey / NiddleResultStore | Typed, one-shot in-memory results between destinations |
| niddleDeepLinks { } | Maps incoming URL patterns to app-owned route keys |

## Graph and entry registration

A graph is immutable after creation. Each graph owns its own start destination. Nested graphs create feature boundaries. A route type can appear only once in the full graph tree.

The graph-aware entry-provider overload checks route ownership before resolving an entry. This catches forgotten registrations close to the source of the problem. The plain niddleEntries { } overload remains available when an app intentionally manages route registration elsewhere.

Graph metadata does not replace Navigation 3's entry provider. UI registration still uses the official entry<Route> DSL, so Navigation 3 remains the source of truth for rendering.

## State restoration

Navigation 3's rememberNavBackStack is the persistence owner. EasyNiddle provides niddleSavedStateConfiguration { } to make the required polymorphic NavKey registrations easy to declare in common code.

- Every concrete route type that may appear in the stack must be @Serializable and registered with route(YourRoute.serializer()).
- Route arguments should be stable, serializable values such as IDs, strings, numbers, and enums.
- Do not put UI objects, ViewModels, platform contexts, or large domain objects in route keys.
- The configured back stack is designed to survive supported configuration changes and process recreation through Navigation 3's saveable state mechanism.
- Common tests verify polymorphic route-stack serialization round trips. Actual host lifecycle restoration should also be exercised in Android/iOS app integration tests because the saveable state registry is provided by the host Compose runtime.

The helper rejects duplicate serializer names early, before a host starts navigating.

## Deep links

NiddleDeepLinkRegistry maps incoming URLs to route objects without depending on Android intents, UIKit, browser APIs, or platform URI classes.

- Register a pattern with deepLink("https://example.com/users/{userId}") { ... }.
- Placeholders must occupy a complete path segment and must have unique names.
- Path and query values are percent-decoded; query '+' is treated as a space.
- Scheme and authority matching are case-insensitive; path matching is case-sensitive.
- Static path segments take priority over placeholders.
- Equal-priority overlapping patterns fail as ambiguous instead of relying on registration order.
- resolve(url) returns null for an unknown URL; requireResolve(url) throws a readable error.
- The host app still registers OS/browser URL handlers and passes received URLs to the registry.

This keeps URL parsing common and testable while leaving platform lifecycle integration to the app.

## Destination results

NiddleResultStore provides a small typed hand-off for short-lived results such as a selected user, picked image URI, or confirmation value.

- Define a stable NiddleResultKey<T> and share that exact key instance between destinations.
- Create one store per navigation host or feature flow; avoid global singleton stores.
- set(key, value) replaces any still-pending value for that key.
- consume(key) returns and removes the value, so delivery is one-shot.
- clear(key) and clearAll() support cancellation and scope cleanup.
- The store is in-memory, not process-restorable, and intended for UI/navigation-thread access.

This is deliberately not a global event bus, coroutine channel, or persistence layer. Apps remain responsible for deciding when the previous destination checks the result (for example, when it becomes active again).

## State ownership

- The app owns its route types and serializer configuration.
- Each host owns its back stack, controller, and result store.
- Each bottom-navigation tab owns an independent history through NiddleMultiStack.
- No global navigator, singleton event bus, or hidden mutable navigation state is introduced.

## Error handling

Fail fast for developer mistakes: missing graph start destinations, duplicate route registrations, unknown routes passed to a graph-aware entry provider, and unknown tab identifiers. Error messages should name the route or tab involved.

## Platform compatibility

The library targets Android, iOS device, iOS simulators including Intel iosX64, Desktop JVM, and WasmJs. Keep platform targets explicit and validate dependency publication before upgrading Kotlin, Compose Multiplatform, or Navigation 3.
