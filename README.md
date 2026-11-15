<p align="center">
  <img src="https://raw.githubusercontent.com/Kratos1996/EasyNiddle/main/easyniddle/libSample/ChatGPT%20Image%20Jun%2027,%202026,%2004_59_45%20PM.png" alt="EasyNiddle — typed navigation made simple" width="100%">
</p>

<h1 align="center">EasyNiddle KMP</h1>
<p align="center">
  <strong>Easy, type-safe navigation for Compose Multiplatform.</strong><br>
  One friendly API for Android, iOS, Desktop and Web — powered by Navigation 3.
</p>

<p align="center">
  <a href="https://github.com/Kratos1996/EasyNiddle-KMP/actions"><img src="https://img.shields.io/github/actions/workflow/status/Kratos1996/EasyNiddle-KMP/ci.yml?branch=main&label=CI" alt="CI status"></a>
  <img src="https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin 2.3.20">
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-1.10.3-4285F4" alt="Compose Multiplatform">
  <img src="https://img.shields.io/badge/Navigation%203-KMP-0A7D55" alt="Navigation 3">
  <a href="https://github.com/Kratos1996/EasyNiddle-KMP"><img src="https://img.shields.io/github/stars/Kratos1996/EasyNiddle-KMP?style=social" alt="GitHub stars"></a>
</p>

<p align="center">
  <a href="docs/QUICK_START.md">Quick start</a> ·
  <a href="docs/COOKBOOK.md">Cookbook</a> ·
  <a href="docs/MIGRATION.md">Migration guide</a> ·
  <a href="docs/ROADMAP.md">Roadmap</a>
</p>

---

## Why EasyNiddle?

Navigation should not take more code than the screens themselves. EasyNiddle gives Compose Multiplatform developers a readable, type-safe API while keeping Navigation 3 underneath.

It brings the familiar developer experience from the original [EasyNiddle for Android](https://github.com/Kratos1996/EasyNiddle) into a multiplatform library — without bringing Android-only APIs into common code.

## Quick example

Define serializable routes once:

```kotlin
@Serializable
sealed interface AppRoute : SerializableRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data class Profile(val userId: String) : AppRoute
}
```

Register the sealed route serializer once:

```kotlin
val appState = niddleSavedStateConfiguration {
    route(AppRoute.serializer())
}
```

Create your host with a screen DSL:

```kotlin
@Composable
fun AppNavigation() {
    val navigator = remember { NiddleNavigator() }

    NiddleNavHost<AppRoute>(
        startDestination = AppRoute.Home,
        navigator = navigator,
        savedStateConfiguration = appState,
    ) {
        screen<AppRoute.Home> {
            val nav = LocalNiddleNavigator.current
            HomeScreen(onOpenProfile = { nav.navigate(AppRoute.Profile("42")) })
        }

        screen<AppRoute.Profile> { route ->
            ProfileScreen(userId = route.userId)
        }
    }
}
```

Navigate from any child composable:

```kotlin
val navigator = LocalNiddleNavigator.current

navigator.navigate(AppRoute.Profile("42"))
navigator.pop()
navigator.popTo(AppRoute.Home)
navigator.navigateSingleTop(AppRoute.Home)
navigator.navigate(AppRoute.Profile("42"), popUpTo = AppRoute.Home, inclusive = false)
```

**No string routes. No manual entry-provider setup for the common case.** For advanced use cases, the lower-level Navigation 3 back-stack and entry-provider APIs remain available.

## Features

| Feature | What it gives you |
|---|---|
| Familiar screen DSL | `NiddleNavHost { screen<Route> { } }` |
| Type-safe routes | Kotlin objects and data classes instead of string routes |
| Standalone navigator | `NiddleNavigator` can be scoped or injected with your DI framework |
| CompositionLocal | Access navigation with `LocalNiddleNavigator.current` |
| Back-stack commands | Navigate, pop, pop-to, inclusive pop, single-top and pop-up-to |
| Destination results | Send one-shot results back with `navigateBackWithResult` |
| Bottom navigation | Simple bottom-bar command plus independent `NiddleMultiStack` histories |
| Transitions | Configurable push, pop and predictive-back animations |
| Navigation graph | Typed route registration, nested graphs and duplicate checks |
| Deep links | Common URL pattern matching and typed route resolution |
| State restoration | Polymorphic route serializer configuration for Navigation 3 |
| Debug logging | Optional multiplatform log sink through `NiddleLogger` |

## Supported platforms

| Platform | Target |
|---|---|
| Android | Android KMP library target |
| iOS device | `iosArm64` |
| Apple Silicon simulator | `iosSimulatorArm64` |
| Intel simulator | `iosX64` |
| Desktop | JVM |
| Web | WasmJs |

**The Intel `iosX64` target is intentionally retained.** The current dependency baseline is chosen to keep that target available.

## Installation

The multiplatform artifact is not published to Maven Central yet. Until the first KMP release, add the module directly to your project:

```kotlin
dependencies {
    implementation(project(":easyniddle"))
}
```

Publishing coordinates and signed releases will be documented here when the KMP artifact is available. The original Android artifact is a separate project and release line.

## More examples and guides

- [Quick start](docs/QUICK_START.md) — routes, saved state, screen DSL and navigation.
- [Cookbook](docs/COOKBOOK.md) — bottom navigation, results, deep links and custom transitions.
- [Architecture](docs/ARCHITECTURE.md) — state ownership and Navigation 3 integration.
- [Migration guide](docs/MIGRATION.md) — move from Navigation Compose or the original EasyNiddle API.
- [Compatibility policy](docs/COMPATIBILITY.md) — target and dependency decisions.
- [Roadmap](docs/ROADMAP.md) — current status and remaining work.

## Build and verification

CI uses Java 21 and Gradle 9.3.0. It checks Android, Desktop JVM, WasmJs, iOS including `iosX64`, Desktop sample compilation and common tests, the minified Android consumer build with R8, and Kotlin ABI compatibility.

Run the sample after the Gradle Wrapper is present:

```bash
./gradlew :sample:run
```

## Known limitations

- Host-level Android/iOS process recreation integration tests are still planned. Common serializer round-trip tests do not replace lifecycle tests in a real app.
- Platform App Links, Universal Links and browser URL delivery are configured by the host application.
- `NiddleResultStore` and the command-based result stream are in-memory; they are not restored after process death.
- Maven Central publishing and signed KMP releases are not configured yet.

---

## Created and maintained by

<p align="center">
  <img src="https://github.com/Kratos1996.png?size=240" alt="Ishant Sharma" width="140" style="border-radius:50%">
</p>

<h3 align="center">Ishant Sharma</h3>
<p align="center">
  <strong>Creator & Maintainer of EasyNiddle</strong><br>
  Kotlin · Android · Compose Multiplatform · Fintech · Web3
</p>
<p align="center">
  <a href="https://github.com/Kratos1996">GitHub Profile</a> ·
  <a href="https://github.com/Kratos1996/EasyNiddle">Original Android Library</a> ·
  <a href="mailto:ishant.sharma1947@gmail.com">Email</a>
</p>

EasyNiddle started as a small effort to make Jetpack Navigation 3 easier to use. This KMP edition continues that idea across platforms: simple APIs for everyday navigation, with the full Navigation 3 foundation available when you need more control.

**Built with Kotlin and Compose Multiplatform.**
