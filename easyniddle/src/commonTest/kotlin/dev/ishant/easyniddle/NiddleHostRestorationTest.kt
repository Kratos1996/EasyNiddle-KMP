package dev.ishant.easyniddle

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable

@Serializable
private data object HostRestoreHome : NiddleRoute

@Serializable
private data class HostRestoreProfile(val userId: String) : NiddleRoute

class NiddleHostRestorationTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun hostRestoresPolymorphicBackStackAndRouteArgumentsAfterCompositionRecreation() =
        runComposeUiTest {
            val savedStateConfiguration = niddleSavedStateConfiguration {
                route(HostRestoreHome.serializer())
                route(HostRestoreProfile.serializer())
            }
            val restorationTester = StateRestorationTester(this)
            lateinit var backStack: NavBackStack<NavKey>

            restorationTester.setContent {
                backStack = rememberNiddleBackStack(
                    savedStateConfiguration = savedStateConfiguration,
                    startDestination = HostRestoreHome,
                )
                NiddleNavHost(
                    backStack = backStack,
                    entryProvider = niddleEntries {
                        entry<HostRestoreHome> {
                            BasicText("Home")
                        }
                        entry<HostRestoreProfile> { route ->
                            BasicText("Profile: ${route.userId}")
                        }
                    },
                )
            }

            onNodeWithText("Home").assertExists()
            runOnIdle {
                backStack.add(HostRestoreProfile(userId = "user-42"))
            }
            waitForIdle()
            onNodeWithText("Profile: user-42").assertExists()

            restorationTester.emulateSaveAndRestore()
            waitForIdle()

            onNodeWithText("Profile: user-42").assertExists()
            runOnIdle {
                assertEquals(2, backStack.size)
                assertEquals(HostRestoreProfile("user-42"), backStack.last())
                backStack.removeAt(backStack.lastIndex)
            }
            waitForIdle()
            onNodeWithText("Home").assertExists()
        }
}
