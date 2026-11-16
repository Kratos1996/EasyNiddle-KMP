package dev.ishant.easyniddle.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "EasyNiddle KMP") {
        SampleApp()
    }
}
