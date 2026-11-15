# Quick start (early API)

Define serializable routes:

```kotlin
@Serializable data object HomeKey : NiddleRoute
@Serializable data class ProfileKey(val userId: String) : NiddleRoute
```

Register all route serializers in a multiplatform `SavedStateConfiguration` using a `SerializersModule` and the Navigation 3 saved-state integration. Then define entries:

```kotlin
val entries = niddleEntries {
    entry<HomeKey> { HomeScreen() }
    entry<ProfileKey> { key -> ProfileScreen(key.userId) }
}

NiddleNavHost(
    startDestination = HomeKey,
    savedStateConfiguration = appSavedStateConfiguration,
    entryProvider = entries,
)
```

This API is an early adapter. Compile against the resolved JetBrains Navigation 3 KMP artifact and validate all targets before using it in a production release.
