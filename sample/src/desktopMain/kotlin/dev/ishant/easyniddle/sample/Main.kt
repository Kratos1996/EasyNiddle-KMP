package dev.ishant.easyniddle.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.ishant.easyniddle.NiddleController
import dev.ishant.easyniddle.NiddleNavHost
import dev.ishant.easyniddle.NiddleRoute
import dev.ishant.easyniddle.niddleEntries
import dev.ishant.easyniddle.niddleGraph
import dev.ishant.easyniddle.niddleSavedStateConfiguration
import dev.ishant.easyniddle.rememberNiddleBackStack
import kotlinx.serialization.Serializable

@Serializable
private data object HomeRoute : NiddleRoute

@Serializable
private data class ProfileRoute(val userId: String) : NiddleRoute

@Serializable
private data object SettingsRoute : NiddleRoute

private val appGraph = niddleGraph(startDestination = HomeRoute) {
    destination<HomeRoute>()
    destination<ProfileRoute>()
    destination<SettingsRoute>()
}

private val savedStateConfiguration = niddleSavedStateConfiguration {
    route(HomeRoute.serializer())
    route(ProfileRoute.serializer())
    route(SettingsRoute.serializer())
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "EasyNiddle Sample",
    ) {
        SampleApp()
    }
}

@Composable
private fun SampleApp() {
    val backStack = rememberNiddleBackStack(
        savedStateConfiguration = savedStateConfiguration,
        startDestination = HomeRoute,
    )
    val navigator = remember(backStack) { NiddleController(backStack) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NiddleNavHost(
                backStack = backStack,
                entryProvider = niddleEntries(appGraph) {
                    entry<HomeRoute> {
                        HomeScreen(
                            onOpenProfile = { navigator.navigate(ProfileRoute("42")) },
                            onOpenSettings = { navigator.navigate(SettingsRoute) },
                        )
                    }
                    entry<ProfileRoute> { route ->
                        ProfileScreen(
                            userId = route.userId,
                            onBack = { navigator.back() },
                        )
                    }
                    entry<SettingsRoute> {
                        SettingsScreen(onBack = { navigator.back() })
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    SamplePage(
        eyebrow = "EASYNIDDLE / HOME",
        title = "Navigation made simple.",
        description = "A small, type-safe navigation layer built on top of Compose Multiplatform Navigation 3.",
    ) {
        Button(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth()) {
            Text("Open profile")
        }
        OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Open settings")
        }
    }
}

@Composable
private fun ProfileScreen(userId: String, onBack: () -> Unit) {
    SamplePage(
        eyebrow = "DESTINATION / PROFILE",
        title = "Profile $userId",
        description = "This screen receives a serializable, typed route argument.",
    ) {
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Navigate back")
        }
    }
}

@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    SamplePage(
        eyebrow = "DESTINATION / SETTINGS",
        title = "Settings",
        description = "This destination is registered in the same typed navigation graph.",
    ) {
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Navigate back")
        }
    }
}

@Composable
private fun SamplePage(
    eyebrow: String,
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(eyebrow, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text(description, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                content()
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("EasyNiddle • Compose Multiplatform", style = MaterialTheme.typography.labelMedium)
    }
}
