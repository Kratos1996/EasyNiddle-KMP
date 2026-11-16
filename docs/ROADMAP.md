# Roadmap

## Foundation
- [x] KMP source-set layout
- [x] Serializable route marker
- [x] Pure stack operations and common tests
- [x] Navigation 3 host adapter and entry-provider bridge
- [x] Caller-owned back stack for navigation actions
- [x] Independent bottom-tab stacks
- [x] Restore Intel iOS Simulator (`iosX64`) with a compatible dependency baseline
- [x] Verify Android/iOS/Desktop/WasmJs target compilation in CI

## Navigation API
- [x] Typed route graph metadata and duplicate-route diagnostics
- [x] Nested graph ownership and start-destination validation
- [x] Reified route registration for readable graph declarations
- [x] Graph-aware entry provider with unknown-route validation
- [x] Typed one-shot destination results
- [x] Deep-link URL pattern matching and typed route resolution
- [x] Polymorphic route serializer registration helper
- [x] Common route-stack serialization round-trip tests
- [x] Configurable push, pop, and predictive-back transition specs
- [x] Initial source-compatibility facade: `SerializableRoute`, `NiddleNavigator`, `LocalNiddleNavigator`, `screen<Route> { }`, and familiar navigation commands
- [ ] Complete host-level API tests and harden compatibility semantics
- [ ] Host-level process recreation tests on Android and iOS

## Production
- [x] Android/iOS/Desktop/WasmJs CI matrix
- [x] Runnable Compose Desktop sample application
- [x] Kotlin ABI compatibility validation
- [x] Minified Android consumer verification with R8
- [x] Public API KDoc
- [x] Quick start, architecture, migration guide, and cookbook
- [ ] Gradle Wrapper bootstrap and committed wrapper files
- [ ] Maven Central publishing and signed releases
