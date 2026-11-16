<p align="center">
  <img src="https://raw.githubusercontent.com/Kratos1996/EasyNiddle/main/easyniddle/libSample/ChatGPT%20Image%20Jun%2027%2C%202026%2C%2004_59_45%20PM.png" alt="EasyNiddle — Type-safe Compose navigation" width="100%" />
</p>

<h1 align="center">EasyNiddle</h1>

<p align="center">
  <strong>Navigation made simple. Across platforms.</strong><br/>
  A developer-friendly, type-safe navigation layer for Compose Multiplatform, built on Navigation 3.
</p>

<p align="center">
  <a href="https://github.com/Kratos1996/EasyNiddle-KMP/actions/workflows/ci.yml"><img src="https://github.com/Kratos1996/EasyNiddle-KMP/actions/workflows/ci.yml/badge.svg?branch=main" alt="CI"/></a>
  <a href="https://github.com/Kratos1996/EasyNiddle-KMP/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-Apache--2.0-blue.svg" alt="License"/></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF.svg?logo=kotlin" alt="Kotlin Multiplatform"/></a>
  <a href="https://developer.android.com/compose"><img src="https://img.shields.io/badge/UI-Compose-4285F4.svg" alt="Compose"/></a>
</p>

---

## Why EasyNiddle?

Navigation should be one of the easiest parts of building an app—not a source of repetitive setup.

**EasyNiddle** is an open-source navigation convenience library created by **Ishant Sharma**. It builds on official Navigation 3 APIs and adds a clean, typed developer experience: route definitions, screen registration, navigation commands, independent tab histories, deep links, results, and configurable transitions.

The project continues the original EasyNiddle idea for Jetpack Compose while extending the architecture to **Compose Multiplatform**.

## At a glance

| Capability | What it gives you |
|---|---|
| Type-safe routes | Kotlin route objects and data classes instead of string route names |
| Familiar screen DSL | Register screens with `screen<Route> { }` |
| Simple navigator | Navigate, pop, pop to, single top, and pop-up-to commands |
| Navigation results | Send a value back to the previous destination |
| Bottom navigation | Maintain independent histories for multiple tabs |
| Nested graphs | Organize route ownership by feature |
| Deep links | Resolve URL patterns into typed routes |
| Saved-state configuration | Register route serializers for Navigation 3 stack persistence |
| Transitions | Customize push, pop, and predictive-back visuals |
| Multiplatform core | Share navigation logic across supported targets |

## Supported platforms

- **Android**
- **iOS device** — `iosArm64`
- **iOS simulators** — `iosSimulatorArm64` and **`iosX64` (Intel)**
- **Desktop** — JVM
- **Web** — `wasmJs`

> The Intel simulator target is intentional. Please keep the dependency versions aligned with the compatibility notes in [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md).

## Installation

EasyNiddle is available via [JitPack](https://jitpack.io/#Kratos1996/EasyNiddle-KMP).

### 1. Add the JitPack repository to your root `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add the dependency

```kotlin
dependencies {
    implementation("com.github.Kratos1996.EasyNiddle-KMP:easyniddle:0.1.1")
}
```

## Quick start — familiar EasyNiddle style

### 1. Define typed routes

```kotlin
@Serializable
sealed interface AppRoute : SerializableRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data class Details(val itemId: String) : AppRoute

    @Serializable
    data object Profile : AppRoute
}
```

`SerializableRoute` is the familiar route marker from the original EasyNiddle API. In KMP, route persistence uses **kotlinx.serialization**—not Java serialization.

### 2. Register route serializers

For process/state restoration, register every concrete route that can enter the back stack:

```kotlin
val appSavedState = niddleSavedStateConfiguration {
    route(AppRoute.Home.serializer())
    route(AppRoute.Details.serializer())
    route(AppRoute.Profile.serializer())
}
```

### 3. Create the host with the screen DSL

```kotlin
val navigator = remember { NiddleNavigator() }

NiddleNavHost<AppRoute>(
    startDestination = AppRoute.Home,
    navigator = navigator,
    savedStateConfiguration = appSavedState,
) {
    screen<AppRoute.Home> {
        HomeScreen()
    }

    screen<AppRoute.Details> { route ->
        DetailsScreen(itemId = route.itemId)
    }

    screen<AppRoute.Profile> {
        ProfileScreen()
    }
}
```

### 4. Navigate from any child composable

```kotlin
@Composable
fun HomeScreen() {
    val navigator = LocalNiddleNavigator.current

    Button(onClick = {
        navigator.navigate(AppRoute.Details(itemId = "42"))
    }) {
        Text("Open details")
    }
}
```

## Navigation commands

```kotlin
val navigator = LocalNiddleNavigator.current

navigator.navigate(AppRoute.Profile)
navigator.navigateSingleTop(AppRoute.Home)
navigator.navigate(AppRoute.Profile, popUpTo = AppRoute.Home, inclusive = false)
navigator.pop()
navigator.popTo(AppRoute.Home)
navigator.popTo(AppRoute.Home, inclusive = true)
navigator.navigateBottomBar(AppRoute.Profile)
navigator.finish()
```

- `navigateSingleTop` returns to an existing matching route when present; otherwise it adds the destination.
- `popTo` supports inclusive removal while protecting the root entry.
- `navigateBottomBar` is a simple root-retaining tab switch. For independent per-tab back histories, use `NiddleMultiStack`.

## Results between destinations

The compatibility host supports a one-shot result callback:

```kotlin
NiddleNavHost<AppRoute>(
    startDestination = AppRoute.Home,
    navigator = navigator,
    savedStateConfiguration = appSavedState,
    onResult = { result ->
        // Handle the returned value in the host
    },
) {
    screen<AppRoute.Home> { HomeScreen() }
    screen<AppRoute.Details> { route -> DetailsScreen(itemId = route.itemId) }
}

// From the destination that is closing:
LocalNiddleNavigator.current.navigateBackWithResult("Completed")
```

For explicitly typed result keys and feature-scoped storage, use `NiddleResultStore` and `NiddleResultKey<T>`. Results are in-memory and are not restored after process death.

## Advanced APIs

The original-style host is an easy entry point. Larger apps can use the lower-level Navigation 3 facade when they need explicit state ownership:

- `NiddleRoute` and `niddleGraph { }` for validated route graphs.
- `rememberNiddleBackStack(...)` and `NiddleController` for caller-owned stack control.
- `niddleEntries(graph) { entry<Route> { ... } }` for graph-aware entry registration.
- `NiddleMultiStack` for independent bottom-navigation back stacks.
- `niddleDeepLinks { deepLink("https://example.com/path/{id}") { ... } }` for typed URL resolution.
- `NiddleResultStore` for typed one-shot results.
- `niddleSlideInTransition`, `niddleSlidePopTransition`, and `niddlePredictiveFadeTransition` for transition defaults.

For details and platform caveats, read the [Quick start](docs/QUICK_START.md), [Cookbook](docs/COOKBOOK.md), [Architecture](docs/ARCHITECTURE.md), and [Migration guide](docs/MIGRATION.md).

## Built by Ishant Sharma

<p align="center">
  <img src="https://github.com/Kratos1996.png" alt="Ishant Sharma GitHub avatar" width="112" />
</p>

<p align="center">
  <strong>Ishant Sharma</strong><br/>
  Android Engineer · Kotlin Multiplatform · Fintech · Web3
</p>

EasyNiddle is an independent open-source project designed and developed by **Ishant Sharma**.

- GitHub: [@Kratos1996](https://github.com/Kratos1996)
- LinkedIn: [Ishant Sharma](https://www.linkedin.com/in/ishant-sharma/)
- Project repository: [EasyNiddle-KMP](https://github.com/Kratos1996/EasyNiddle-KMP)
- Original Android library: [EasyNiddle](https://github.com/Kratos1996/EasyNiddle)

*Built with Kotlin, Compose Multiplatform, and a lot of care for developer experience.*

## Contributing

Issues, bug reports, and pull requests are welcome. Please include the target platform, Kotlin/Compose versions, a minimal reproduction, and the expected versus actual behavior.

## License

EasyNiddle is distributed under the [Apache License 2.0](https://github.com/Kratos1996/EasyNiddle-KMP/blob/main/LICENSE).

---

<p align="center">
  <sub>© Ishant Sharma · EasyNiddle · Open source</sub>
</p>
