# Quick start

EasyNiddle offers two ways to build a navigation host:

1. **Familiar API:** `NiddleNavHost(startDestination) { screen<Route> { } }` with `NiddleNavigator`.
2. **Explicit API:** caller-owned Navigation 3 back stack, `NiddleGraph`, `niddleEntries`, and `NiddleController`.

Use the familiar API when migrating an app from the original Android EasyNiddle library. Use the explicit API when you want to own the stack lifecycle and navigation wiring.

## 1. Define routes

```kotlin
@Serializable
sealed interface AppRoute : SerializableRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data class Profile(val userId: String) : AppRoute

    @Serializable
    data object Settings : AppRoute
}
```

The KMP marker `SerializableRoute` extends `NiddleRoute`. Route state uses kotlinx.serialization. It does not use Java serialization.

## 2. Register serializers

```kotlin
val appSavedState = niddleSavedStateConfiguration {
    route(AppRoute.Home.serializer())
    route(AppRoute.Profile.serializer())
    route(AppRoute.Settings.serializer())
}
```

Every concrete route that can be added to the stack must be registered. Do not rely on automatic serializer discovery.

## 3. Use the familiar screen DSL

```kotlin
val navigator = remember { NiddleNavigator() }

NiddleNavHost<AppRoute>(
    startDestination = AppRoute.Home,
    navigator = navigator,
    savedStateConfiguration = appSavedState,
) {
    screen<AppRoute.Home> { HomeScreen() }
    screen<AppRoute.Profile> { route -> ProfileScreen(route.userId) }
    screen<AppRoute.Settings> { SettingsScreen() }
}
```

Inside any destination:

```kotlin
val navigator = LocalNiddleNavigator.current
navigator.navigate(AppRoute.Profile("user-42"))
navigator.pop()
```

The host exposes `onBack`, `onFinish`, `onNavigateCustom`, and `onResult` callbacks for platform/app behavior.

## 4. Navigation commands

```kotlin
navigator.navigate(AppRoute.Profile("42"))
navigator.navigate(AppRoute.Settings, popUpTo = AppRoute.Home, inclusive = false)
navigator.navigateSingleTop(AppRoute.Home)
navigator.pop()
navigator.popTo(AppRoute.Home)
navigator.navigateBackWithResult("saved")
```

`navigateBottomBar` is a convenience for a simple root-retaining tab switch. Use `NiddleMultiStack` if each tab must preserve its own complete history.

## 5. Advanced caller-owned host

For explicit state ownership, configure a Navigation 3 stack and register entries:

```kotlin
val graph = niddleGraph(startDestination = HomeRoute) {
    destination<HomeRoute>()
    destination<ProfileRoute>()
}

val savedState = niddleSavedStateConfiguration {
    route(HomeRoute.serializer())
    route(ProfileRoute.serializer())
}

val backStack = rememberNiddleBackStack(savedState, HomeRoute)
val controller = remember(backStack) { NiddleController(backStack) }
val entries = niddleEntries(graph) {
    entry<HomeRoute> { HomeScreen { controller.navigate(ProfileRoute("42")) } }
    entry<ProfileRoute> { route -> ProfileScreen(route.userId) }
}

NiddleNavHost(backStack = backStack, entryProvider = entries)
```

## 6. More recipes

See [Cookbook](COOKBOOK.md) for bottom tabs, nested graphs, deep links, result passing, and transition customization.

## Limitations to know

- Host-level process recreation tests on Android/iOS are still planned; common serializer round-trip tests are not a substitute for app lifecycle tests.
- Register platform App Links/Universal Links/browser handlers in the host application; EasyNiddle resolves URLs to routes but does not register OS handlers.
- `NiddleResultStore` and the compatibility `onResult` flow are in-memory; do not use them for durable data.
