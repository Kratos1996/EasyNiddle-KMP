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
| NiddleNavHost() | Displays the current stack through Navigation 3 |
| NiddleController | Readable push, back, single-top, replace, and pop-to operations |
| NiddleMultiStack | Independent histories for bottom-navigation tabs |

## Graph and entry registration

A graph is immutable after creation. Each graph owns its own start destination. Nested graphs create feature boundaries. A route type can appear only once in the full graph tree.

The graph-aware entry-provider overload checks route ownership before resolving an entry. This catches forgotten registrations close to the source of the problem. The plain niddleEntries { } overload remains available when an app intentionally manages route registration elsewhere.

Graph metadata does not replace Navigation 3's entry provider. UI registration still uses the official entry<Route> DSL, so Navigation 3 remains the source of truth for rendering.

## State ownership

- The app owns its route types and serializer configuration.
- Each host owns its back stack and controller.
- Each bottom-navigation tab owns an independent history through NiddleMultiStack.
- No global navigator, singleton event bus, or hidden mutable navigation state is introduced.

## Error handling

Fail fast for developer mistakes: missing graph start destinations, duplicate route registrations, unknown routes passed to a graph-aware entry provider, and unknown tab identifiers. Error messages should name the route or tab involved.

## Platform compatibility

The library targets Android, iOS device, iOS simulators including Intel iosX64, Desktop JVM, and WasmJs. Keep platform targets explicit and validate dependency publication before upgrading Kotlin, Compose Multiplatform, or Navigation 3.
