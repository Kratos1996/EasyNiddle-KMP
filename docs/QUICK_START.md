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

    val appGraph = niddleGraph(startDestination = HomeRoute) {
        destination<HomeRoute>()
        destination<ProfileRoute>()

        nestedGraph(startDestination = SettingsRoute) {
            destination<SettingsRoute>()
        }
    }

A graph validates that each route type is registered once and that each graph's start route belongs to that graph.

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

## 5. Configure transitions and predictive back

The host provides sensible horizontal push/pop transitions and a predictive-back fade by default. Navigation 3 owns the gesture lifecycle and drives the predictive transform interactively.

    NiddleNavHost(
        backStack = backStack,
        entryProvider = entries,
        transitionSpec = niddleSlideInTransition,
        popTransitionSpec = niddleSlidePopTransition,
        predictivePopTransitionSpec = niddlePredictiveFadeTransition,
    )

You can supply your own Compose Animation transition lambdas for all three directions. The predictive lambda receives the swipe edge from Navigation 3; it should return a transform rather than manually modifying the back stack.

## 6. Use named navigation operations

    val navigator = NiddleController(backStack)

    navigator.navigate(ProfileRoute("42"))
    navigator.navigateSingleTop(SettingsRoute)
    navigator.back()
    navigator.popTo(HomeRoute)

The controller is a small convenience facade over the same Navigation 3 back stack—not a second navigation engine.

## 7. Resolve incoming deep links

    val deepLinks = niddleDeepLinks {
        deepLink("https://example.com/users/{userId}") { match ->
            ProfileRoute(match.pathParameter("userId"))
        }
    }

    val route = deepLinks.requireResolve(incomingUrl)
    backStack.add(route)

EasyNiddle resolves the URL into a route; platform-specific app-link registration and receiving the URL remain the host application's responsibility.

## 8. Exchange one-shot destination results

    object AppResultKeys {
        val selectedUser = NiddleResultKey<String>("selected-user")
    }

    val results = NiddleResultStore()
    results.set(AppResultKeys.selectedUser, selectedUserId)
    navigator.back()

    val selectedUserId: String? = results.consume(AppResultKeys.selectedUser)

A result is removed when consumed, so it can be delivered only once. The store is in-memory and scoped to your navigation flow; it is not saved across process death.
