# Architecture

The application owns navigation state. EasyNiddle supplies typed route contracts, deterministic stack policies, and a thin Navigation 3 integration. There is no process-global navigator singleton.

## Layers
1. Route model — serializable `NiddleRoute` keys.
2. Back-stack policy — pure operations with explicit root and duplicate semantics.
3. Graph DSL — planned typed route-to-content registration.
4. Host adapter — Navigation 3 `NavDisplay` integration.
5. Platform adapters — back dispatch and platform integrations isolated by source set.

Each host owns its own stack unless the app explicitly supplies one. Bottom-navigation tabs will each retain an independent stack. Results will be typed, destination-scoped, lifecycle-aware, and consumed once; no global `SharedFlow<Any>` broadcast.

Common code must not depend on Android Activity, Context, AppCompat, Java serialization, or platform-only lifecycle APIs.
