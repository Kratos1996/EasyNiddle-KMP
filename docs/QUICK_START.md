# Quick start (early API)

## 1. Define serializable routes

```kotlin
@Serializable
data object HomeKey : NiddleRoute

@Serializable
data class ProfileKey(val userId: String) : NiddleRoute
```

## 2. Register route serializers

Create a `SavedStateConfiguration` from `androidx.savedstate.serialization` and register all concrete route serializers in its `SerializersModule`. This explicit registration is required for multiplatform restoration.

## 3. Create a back stack and entries

```kotlin
val backStack = rememberNiddleBackStack(
    savedStateConfiguration = appSavedStateConfiguration,
    startDestination = HomeKey,
)

val entries = niddleEntries {
    entry<HomeKey> {
        HomeScreen(onOpenProfile = { backStack.add(ProfileKey("42")) })
    }
    entry<ProfileKey> { key ->
        ProfileScreen(userId = key.userId)
    }
}

NiddleNavHost(
    backStack = backStack,
    entryProvider = entries,
)
```

The app owns the stack, making navigation explicit and testable. The `NiddleController(backStack)` facade offers common push, back, pop-to, single-top, and replace operations.
