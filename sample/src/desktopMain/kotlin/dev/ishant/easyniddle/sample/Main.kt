package dev.ishant.easyniddle.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.ishant.easyniddle.LocalNiddleNavigator
import dev.ishant.easyniddle.NiddleNavHost
import dev.ishant.easyniddle.NiddleNavigator
import dev.ishant.easyniddle.SerializableRoute
import dev.ishant.easyniddle.niddleSavedStateConfiguration
import kotlinx.serialization.Serializable

@Serializable
private sealed interface AppRoute : SerializableRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data class Profile(val userId: String) : AppRoute

    @Serializable
    data object Settings : AppRoute
}

private val appState = niddleSavedStateConfiguration {
    route(AppRoute.serializer())
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "EasyNiddle KMP") {
        SampleApp()
    }
}

@Composable
private fun SampleApp() {
    val navigator = remember { NiddleNavigator() }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NiddleNavHost<AppRoute>(
                startDestination = AppRoute.Home,
                navigator = navigator,
                savedStateConfiguration = appState,
            ) {
                screen<AppRoute.Home> {
                    val nav = LocalNiddleNavigator.current
                    SamplePage(
                        title = "Easy navigation.",
                        subtitle = "A small type-safe API over Compose Multiplatform Navigation 3.",
                    ) {
                        Button(
                            onClick = { nav.navigate(AppRoute.Profile("42")) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Open profile") }
                        Button(
                            onClick = { nav.navigate(AppRoute.Settings) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Open settings") }
                    }
                }
                screen<AppRoute.Profile> { route ->
                    val nav = LocalNiddleNavigator.current
                    SamplePage(
                        title = "Profile ${route.userId}",
                        subtitle = "The route argument is delivered as a typed data class.",
                    ) {
                        Button(onClick = { nav.pop() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Navigate back")
                        }
                        Button(
                            onClick = { nav.navigateSingleTop(AppRoute.Home) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Go home (single top)") }
                    }
                }
                screen<AppRoute.Settings> {
                    val nav = LocalNiddleNavigator.current
                    SamplePage(
                        title = "Settings",
                        subtitle = "This screen was declared with screen<Route> { }.",
                    ) {
                        Button(onClick = { nav.pop() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Navigate back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SamplePage(
    title: String,
    subtitle: String,
    content: @Composable Column.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyLarge)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
        Text("EasyNiddle • Compose Multiplatform", style = MaterialTheme.typography.labelMedium)
    }
}
