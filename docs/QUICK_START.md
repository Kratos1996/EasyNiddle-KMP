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

## 5. Exchange one-shot destination results

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

## 6. Save and restore routes

Create SavedStateConfiguration with serializers for your concrete route types in its SerializersModule. Every route used in a restorable back stack must have a serializer registered. Keep route classes serializable and prefer stable, primitive route arguments.
