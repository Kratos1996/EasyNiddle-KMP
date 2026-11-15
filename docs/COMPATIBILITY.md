# Compatibility policy

EasyNiddle keeps the `iosX64` Kotlin/Native target because Intel-based iOS Simulator builds are an explicit project requirement.

This target was removed from Compose Multiplatform starting with 1.11.1. Therefore, the project intentionally pins the last stable Compose Multiplatform line that still supports it:

- Kotlin: 2.3.20
- Compose Multiplatform: 1.10.3
- JetBrains Navigation 3 KMP: 1.0.0-alpha06

The Navigation 3 version is aligned with the Compose Multiplatform 1.10.3 release train. Do not upgrade Compose Multiplatform to 1.11.1+ or Navigation 3 independently without first validating that the full dependency graph publishes an `iosX64` variant.

CI must resolve the full multiplatform dependency graph and run the Desktop tests. A release should additionally compile Android, iOS device, iOS simulator (including `iosX64`), Desktop, and WasmJs targets.
