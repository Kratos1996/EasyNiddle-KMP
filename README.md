# EasyNiddle

A developer-friendly, type-safe navigation layer for Compose Multiplatform, built on Navigation 3.

> Early development. Public APIs are not stable yet.

## Targets
- Android
- iOS
- Desktop JVM
- Web (WasmJs)

## Principles
- Small, discoverable API for common navigation.
- App-owned back stack; no hidden global singleton.
- Serializable, type-safe route keys.
- Multiplatform-first common code (no Android `Context`, `Activity`, or Java serialization).
- Explicit stack behavior and independently testable operations.
- Escape hatches to the underlying Navigation 3 primitives.

## Project layout
- `easyniddle`: reusable multiplatform library.
- `sample`: planned consumer sample (added in the next implementation step).

## Status
This is the initial source foundation. The Navigation 3 adapter and public DSL are early APIs and require Gradle compilation against the selected KMP artifacts before being considered stable.
