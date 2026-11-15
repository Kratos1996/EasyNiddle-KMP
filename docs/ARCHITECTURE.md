# Architecture

EasyNiddle is a Kotlin Multiplatform convenience layer over official Navigation 3 primitives. It does not introduce a second navigation engine.

## Core principles
- Navigation 3 owns rendering and the observable back stack.
- Applications own route keys and may own the back stack.
- Route keys implement `NiddleRoute` and are serializable for state restoration.
- `NiddleController` provides explicit stack operations.
- `NiddleMultiStack` keeps independent histories for top-level destinations.
- `NiddleGraph` validates typed route ownership, nested graph boundaries, and start destinations. Entry rendering remains registered through Navigation 3's official entry-provider API.

## Graph validation
A graph declares a start route, registered route classes, and optional nested graphs. Duplicate route classes are rejected across the complete graph tree. A graph's start destination must be registered in that graph. Nested graphs own their own start destination and can be queried through the root graph's `contains` and `routeTypes` APIs.

## State and lifecycle
Back-stack persistence uses Navigation 3's saved-state integration. Destination UI state is preserved by Navigation 3's saveable entry decorator. Platform-specific lifecycle behavior must be validated per target before release.
