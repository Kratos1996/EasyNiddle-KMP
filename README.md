# EasyNiddle

A developer-friendly, type-safe navigation layer for Compose Multiplatform, built on Navigation 3.

> Early development. Public APIs are not stable yet.

## Targets
- Android
- iOS (iosArm64, iosSimulatorArm64, iosX64)
- Desktop JVM
- Web (WasmJs)

## Principles
- Small, discoverable API for common navigation.
- App-owned back stack; no hidden global singleton.
- Serializable, type-safe route keys.
- Multiplatform-first common code (no Android `Context`, `Activity`, or Java serialization).
- Explicit stack behavior and independently testable operations.
- Escape hatches to the underlying Navigation 3 primitives.

## Current features
- Navigation 3 host adapter and official entry-provider bridge.
- Typed route graphs, nested graph ownership, and route registration validation.
- Per-host navigation controller convenience API.
- Independent, snapshot-aware bottom-navigation stacks.
- Typed one-shot destination results.
- Multiplatform deep-link pattern matching with typed route resolution.
- Multiplatform SavedStateConfiguration helper for polymorphic route registration.
- Deterministic stack operations and common unit tests.

## Sample application

A runnable Compose Desktop sample demonstrates serializable routes, graph registration, the Navigation 3 host, typed route arguments, and controller-based back navigation.

Run it from the repository root:

```bash
gradle :sample:run
```

## Build
The CI workflow installs Gradle 9.3.0, compiles the sample app, and runs the library's Desktop target tests. It also compiles Android, Desktop, WasmJs, iosArm64, iosSimulatorArm64, and iosX64. A Gradle wrapper will be added once the version baseline is finalized.

## Status
Early development. Public APIs and platform behavior are still being validated; host-level process recreation coverage remains planned.
