# Compatibility policy

Use JetBrains-published Kotlin Multiplatform Navigation 3 artifacts, not AndroidX-only artifacts. Initial candidate baseline: Compose Multiplatform 1.12.1 and JetBrains Navigation 3 KMP 1.1.2.

These versions are a candidate baseline, not a claim that every target has been built here. Verify dependency resolution and compile Android, iOS simulator, Desktop, and WasmJs on CI before release. Do not copy AndroidX Navigation 3 version numbers directly into JetBrains KMP coordinates.
