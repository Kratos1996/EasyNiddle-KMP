# Architecture decisions

- Keep Navigation 3 concepts visible; reduce boilerplate rather than create a parallel navigation engine.
- Require explicit polymorphic route serializers for multiplatform restoration.
- Keep navigation state scoped to each host; no global navigator or untyped event bus.
- Add convenience APIs only where semantics are deterministic and covered by tests.
