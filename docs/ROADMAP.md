# Roadmap

## Foundation
- [x] KMP source-set layout
- [x] Serializable route marker
- [x] Pure stack operations and common tests
- [x] Navigation 3 host adapter and entry-provider bridge
- [x] Caller-owned back stack for navigation actions
- [x] Independent bottom-tab stacks
- [x] CI Gradle bootstrap
- [ ] Verify Kotlin/AGP/Compose/Navigation 3 version matrix with green CI

## Navigation API
- [x] Typed route graph metadata and duplicate-route diagnostics
- [x] Nested graph ownership and start-destination validation
- [ ] Typed destination content registration integrated with Navigation 3 entry provider
- [ ] Typed one-shot destination results
- [ ] Deep links
- [ ] State restoration tests
- [ ] Predictive back and custom transitions

## Production
- [ ] Android/iOS/Desktop/WasmJs CI matrix
- [ ] Sample application
- [ ] API compatibility and R8 verification
- [ ] KDoc, migration guide, cookbook
- [ ] Maven Central publishing and signed releases
