# Architecture

The application owns navigation state. EasyNiddle supplies typed route contracts, deterministic stack policies, a Navigation 3 host adapter, and optional per-host conveniences. There is no process-global navigator singleton.

## Layers
1. Route model — serializable `NiddleRoute` keys.
2. Back-stack policy — pure operations with explicit root and duplicate semantics.
3. Multi-stack state — Compose snapshot-aware independent tab histories.
4. Entry mapping — Navigation 3 entry-provider integration.
5. Host adapter — Navigation 3 `NavDisplay` integration.
6. Platform adapters — back dispatch and platform integrations isolated by source set.

## Multiple back stacks
Each tab owns a separate stack. Switching tabs preserves the previous tab's history. Back only mutates the active tab. Reselect behavior is opt-in and can pop the selected tab to its root.

## State ownership
Create one navigation state holder per host. Do not put a navigator in a global singleton. For process recreation, configure Navigation 3's saved-state serializer with every concrete route serializer.

## Results
Results will be typed, destination-scoped, lifecycle-aware, and consumed once; avoid a global `SharedFlow<Any>` broadcast.

Common code must not depend on Android Activity, Context, AppCompat, Java serialization, or platform-only lifecycle APIs.
