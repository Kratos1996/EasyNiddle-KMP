# Quick start

EasyNiddle keeps navigation explicit: define serializable routes, describe the graph, register UI entries, then host the Navigation 3 back stack.

## 1. Define routes

    @Serializable
    data object HomeRoute : NiddleRoute

    @Serializable
    data class ProfileRoute(val userId: String) : NiddleRoute

    @Serializable
    data object SettingsRoute : NiddleRoute

## 2. Describe the graph

Use the reified destination<Route>() API so route registration stays readable.

    val appGraph = niddleGraph(startDestination = HomeRoute) {
        destination<HomeRoute>()
        destination<ProfileRoute>()

        nestedGraph(startDestination = SettingsRoute) {
            destination<SettingsRoute>()
        }
    }

A graph validates that each route type is registered once and that each graph's start route belongs to that graph. Nested graphs make feature boundaries visible in one place.

## 3. Create the back stack and register UI

    val backStack = rememberNiddleBackStack(
        savedStateConfiguration = appSavedStateConfiguration,
        startDestination = appGraph.startDestination,
    )

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

## 4. Use named navigation operations

    val navigator = NiddleController(backStack)

    navigator.navigate(ProfileRoute("42"))
    navigator.navigateSingleTop(SettingsRoute)
    navigator.back()
    navigator.popTo(HomeRoute)

The controller is a small convenience facade over the same Navigation 3 back stack—not a second navigation engine.

## 5. Save and restore routes

Create SavedStateConfiguration with serializers for your concrete route types in its SerializersModule. Every route used in a restorable back stack must have a serializer registered. Keep route classes serializable and prefer stable, primitive route arguments.
