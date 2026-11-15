# Quick start

EasyNiddle keeps navigation explicit: define serializable routes, describe the graph, register UI entries, then host the Navigation 3 back stack.

## 1. Define routes

    @Serializable
    data object HomeRoute : NiddleRoute

    @Serializable
    data class ProfileRoute(val userId: String) : NiddleRoute

    @Serializable
    data object SettingsRoute : NiddleRoute

## 2. Configure multiplatform state restoration

Register every concrete route type that may enter this host's back stack. The helper creates the polymorphic SavedStateConfiguration required by Navigation 3 on iOS, Desktop, and Wasm as well as Android.

    val appSavedStateConfiguration = niddleSavedStateConfiguration {
        route(HomeRoute.serializer())
        route(ProfileRoute.serializer())
        route(SettingsRoute.serializer())
    }

    val backStack = rememberNiddleBackStack(
        savedStateConfiguration = appSavedStateConfiguration,
        startDestination = HomeRoute,
    )

Navigation 3 owns the actual save/restore mechanism. Keep route arguments serializable and stable; avoid storing screens, ViewModels, contexts, or other runtime objects in route keys.

## 3. Describe the graph

Use the reified destination<Route>() API so route registration stays readable.

    val appGraph = niddleGraph(startDestination = HomeRoute) {
        destination<HomeRoute>()
        destination<ProfileRoute>()

        nestedGraph(startDestination = SettingsRoute) {
            destination<SettingsRoute>()
        }
    }

A graph validates that each route type is registered once and that each graph's start route belongs to that graph. Nested graphs make feature boundaries visible in one place.

## 4. Register UI and host navigation

    val entries = niddleEntries(appGraph) {
        entry<HomeRoute> {
            HomeScreen(onOpenProfile = {
                backStack.add(ProfileRoute("42"))
            })
        }
        entry<ProfileRoute> { route ->
            ProfileScreen(userId = route.userId)
        }
        entry<SettingsRoute> {
            SettingsScreen()
        }
    }

    NiddleNavHost(backStack = backStack, entryProvider = entries)

The graph-aware niddleEntries(appGraph) overload rejects unregistered routes before asking Navigation 3 to resolve their UI.

## 5. Use named navigation operations

    val navigator = NiddleController(backStack)

    navigator.navigate(ProfileRoute("42"))
    navigator.navigateSingleTop(SettingsRoute)
    navigator.back()
    navigator.popTo(HomeRoute)

The controller is a small convenience facade over the same Navigation 3 back stack—not a second navigation engine.

## 6. Resolve incoming deep links

Register URL patterns once and map each match to your own serializable route type.

    val deepLinks = niddleDeepLinks {
        deepLink("https://example.com/users/{userId}") { match ->
            ProfileRoute(match.pathParameter("userId"))
        }
    }

    // Call this from your platform entry point after receiving the URL.
    val route = deepLinks.requireResolve(incomingUrl)
    backStack.add(route)

Query values are available through match.queryParameter("source"), and encoded path/query values are decoded. Static paths take priority over parameterized paths. If two matching patterns have equal priority, resolution fails with an ambiguity error instead of silently choosing one.

EasyNiddle resolves the URL into a route; platform-specific app-link registration and receiving the URL remain the host application's responsibility.

## 7. Exchange one-shot destination results

Create typed keys once and reuse the same key instance on both destinations. Keep one result store per navigation host/scope.

    object AppResultKeys {
        val selectedUser = NiddleResultKey<String>("selected-user")
    }

    val results = NiddleResultStore()

    // In the destination that opens the picker:
    navigator.navigate(UserPickerRoute)

    // In UserPickerRoute after the user selects a user:
    results.set(AppResultKeys.selectedUser, selectedUserId)
    navigator.back()

    // In the previous destination when it resumes or is otherwise notified:
    val selectedUserId: String? =
        results.consume(AppResultKeys.selectedUser)

A result is removed when consumed, so it can be delivered only once. Calling set again for the same key replaces its pending value. Keys are identity-based: do not recreate a key with the same name in another destination; share the same key property instead.

The store is in-memory and scoped to your navigation flow. It is not saved across process death and should not replace a database or persistent state holder. Read and write it on the UI/navigation thread.
