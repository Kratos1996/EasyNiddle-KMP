# Compatibility policy

EasyNiddle keeps the `iosX64` Kotlin/Native target because Intel-based iOS Simulator builds are an explicit project requirement.

This target was removed from Compose Multiplatform starting with 1.11.1. Therefore, the project intentionally pins the compatible Compose Multiplatform line:

- Kotlin: 2.3.20
- Compose Multiplatform: 1.10.3
- JetBrains Navigation 3 KMP: 1.0.0-alpha06

Do not upgrade Compose Multiplatform to 1.11.1+ or Navigation 3 independently without first validating that the full dependency graph publishes an `iosX64` variant.

CI validates Desktop tests and compiles Android, Desktop, and WasmJs on Linux. A macOS job compiles iOS device and simulator targets, including `iosX64`. Keep these jobs green before merging dependency or navigation API upgrades.
