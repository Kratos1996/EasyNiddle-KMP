# EasyNiddle

A developer-friendly, type-safe navigation convenience layer for Compose Multiplatform, built on official Navigation 3 primitives.

> **Development status:** early development. Public APIs may change before the first stable release.

## Supported targets

- Android
- iOS device: `iosArm64`
- iOS simulators: `iosSimulatorArm64`, `iosX64` (Intel)
- Desktop JVM
- Web: `wasmJs`

The Intel iOS Simulator target is intentional. The dependency versions are pinned to a compatible Compose Multiplatform line; see [compatibility policy](docs/COMPATIBILITY.md).

## Features

- Navigation 3 host adapter and official entry-provider DSL.
- Serializable, typed route keys and validated nested route graphs.
- Caller-owned back stack with a small `NiddleController` convenience facade.
- Independent, snapshot-aware bottom-navigation histories.
- Typed one-shot in-memory destination results.
- Common deep-link pattern matching and typed route resolution.
- Polymorphic route serializer configuration for Navigation 3 saved-state support.
- Configurable push, pop, and predictive-back transitions.
- Kotlin ABI compatibility validation and an Android minified-consumer R8 smoke build.

## Documentation

| Guide | What it covers |
|---|---|
| [Quick start](docs/QUICK_START.md) | Set up routes, graph, entries, host, and navigation |
| [Architecture](docs/ARCHITECTURE.md) | API responsibilities, state ownership, and platform boundaries |
| [Cookbook](docs/COOKBOOK.md) | Bottom tabs, deep links, results, nested graphs, transitions |
| [Migration guide](docs/MIGRATION.md) | Move an existing Navigation Compose host to EasyNiddle |
| [Compatibility](docs/COMPATIBILITY.md) | Dependency baseline and why `iosX64` is retained |
| [Roadmap](docs/ROADMAP.md) | Completed and planned work |

## Sample

A runnable Compose Desktop sample demonstrates serializable routes, graph registration, Navigation 3 entries, typed route arguments, and controller-based back navigation.

After the Gradle Wrapper is generated in the repository, run:

```bash
./gradlew :sample:run
```

Until then, use an installed Gradle 9.3.0:

```bash
gradle :sample:run
```

## Build and CI

CI uses Gradle 9.3.0 and Java 21. It validates:

- Android, Desktop JVM, and WasmJs compilation.
- iOS device and simulator compilation, including Intel `iosX64`.
- Desktop sample compilation and common library tests.
- Minified Android consumer build with R8.
- Kotlin ABI compatibility against the committed API baseline.

Run the full target matrix and tests through the GitHub Actions workflow. iOS compilation requires macOS.

## Important limitations

- Host-level process recreation tests on Android and iOS are still planned. The common serializer round-trip tests do not replace app-level lifecycle tests.
- Deep-link URL receiving and OS/browser link registration remain the host application's responsibility.
- `NiddleResultStore` is in-memory and is not restored after process death.
- Maven Central publishing and signed release setup are not configured yet.

## Project principles

- Keep Navigation 3 visible rather than creating a parallel navigation engine.
- Keep navigation state scoped to each host; no global navigator singleton.
- Keep route keys small, immutable, and serializable.
- Keep common code free from Android `Context`, `Activity`, and Java serialization.
- Prefer deterministic APIs with common tests and explicit platform validation.
