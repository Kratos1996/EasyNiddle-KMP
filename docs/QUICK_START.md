# Quick start

EasyNiddle has two usage styles. Start with the compact compatibility DSL for a typical app. Use the lower-level API when you need complete ownership of the back stack and graph.

## 1. Define serializable routes

A sealed route family is the simplest pattern. The original Android name `SerializableRoute` is available as a multiplatform marker; it uses kotlinx.serialization, not Java serialization.

```kotlin
@Serializable
sealed interface AppRoute : SerializableRoute {
    @Serializable data object Home : AppRoute
    @Serializable data class Profile(val userId: String) : AppRoute
    @Serializable data object Settings : AppRoute
}
```

## 2. Register state serializers once

For a sealed route interface/class, register its generated serializer. Its annotated sealed subtypes are handled by that serializer.

```kotlin
val appState = niddleSavedStateConfiguration {
    route(AppRoute.serializer())
}
```

For unrelated route classes, register each concrete serializer separately.

## 3. Use the simple host and screen DSL

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
            HomeScreen(
                onProfileClick = { nav.navigate(AppRoute.Profile("42")) },
                onSettingsClick = { nav.navigate(AppRoute.Settings) },
            )
        }

        screen<AppRoute.Profile> { route ->
            ProfileScreen(userId = route.userId)
        }

        screen<AppRoute.Settings> {
            SettingsScreen()
        }
    }
}
```

The same navigator instance can be provided by Hilt, Koin or your own DI setup. When omitted, the host remembers a navigator for that host.

## 4. Navigate from any child composable

```kotlin
val navigator = LocalNiddleNavigator.current

navigator.navigate(AppRoute.Profile("42"))
navigator.pop()
navigator.popTo(AppRoute.Home)
navigator.popTo(AppRoute.Home, inclusive = true)
navigator.navigateSingleTop(AppRoute.Home)
navigator.navigate(AppRoute.Settings, popUpTo = AppRoute.Home, inclusive = false)
```

`navigateSingleTop` returns to the last matching route if one exists; otherwise it adds the route. `popTo` preserves the root and does not empty the stack.

## 5. Receive a result

Collect the same navigator's result stream:

```kotlin
val result by navigator.navigationResult.collectAsState(initial = null)
```

From the result destination:

```kotlin
LocalNiddleNavigator.current.navigateBackWithResult("Saved successfully")
```

You can also use the host's `onResult` callback. Results are in-memory and are not restored after process death.

## 6. Handle platform finish and custom navigation

The common API does not import Android `Activity` or `Context`. Supply the platform behavior from the host:

```kotlin
NiddleNavHost<AppRoute>(
    startDestination = AppRoute.Home,
    navigator = navigator,
    savedStateConfiguration = appState,
    onFinish = { /* close the current platform window/activity */ },
    onNavigateCustom = { route -> /* app-specific handling */ },
) {
    // screen declarations
}
```

## 7. Use the advanced Navigation 3 API when needed

You can still own the back stack directly and use graph validation:

```kotlin
val backStack = rememberNiddleBackStack(appState, AppRoute.Home)
val controller = NiddleController(backStack)

val graph = niddleGraph(startDestination = AppRoute.Home) {
    destination<AppRoute.Home>()
    destination<AppRoute.Profile>()
    destination<AppRoute.Settings>()
}

val entries = niddleEntries(graph) {
    entry<AppRoute.Home> { HomeScreen() }
    entry<AppRoute.Profile> { route -> ProfileScreen(route.userId) }
    entry<AppRoute.Settings> { SettingsScreen() }
}

NiddleNavHost(backStack = backStack, entryProvider = entries)
```

## 8. Custom transitions

```kotlin
NiddleNavHost(
    backStack = backStack,
    entryProvider = entries,
    transitionSpec = niddleSlideInTransition,
    popTransitionSpec = niddleSlidePopTransition,
    predictivePopTransitionSpec = niddlePredictiveFadeTransition,
)
```

Navigation 3 controls the predictive-back gesture; the lambda describes the transition visuals.

## 9. Deep links

```kotlin
val deepLinks = niddleDeepLinks {
    deepLink("https://example.com/users/{userId}") { match ->
        AppRoute.Profile(match.pathParameter("userId"))
    }
}

val route = deepLinks.requireResolve(incomingUrl)
backStack.add(route)
```

Android App Links, iOS Universal Links and browser URL receiving remain the host app's responsibility.

## 10. Independent bottom-tab histories

Use `NiddleMultiStack` when each bottom-navigation tab should retain its own history. `NiddleNavigator.navigateBottomBar` is a simpler root-retaining convenience command, not a replacement for independent tab stacks.

See [Cookbook](COOKBOOK.md) for more recipes.
