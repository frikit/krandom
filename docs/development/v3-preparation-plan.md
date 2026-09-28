# Major-version decision: deferred

The framework review found compatible delivery paths for profile rollback, replay snapshots, and
independent object-field streams. The latest compatible delivery record is
[2.5.0](release-2.5.0-plan.md). Existing defaults and v1 recipe interpretation remain supported.

A 3.0.0 release is not scheduled. Reconsider it only when changing an established default or
removing a deprecated API provides enough consumer benefit to justify migration. Require an exact
contract, before/after fixtures, compile-tested migration and rollback examples, performance
evidence, and full release qualification. Internal cleanup and additive APIs do not require v3.

Since the 2026-09-04 framework review, CI requires the native-image smoke test and the API gate
analyses both versions with their dependency classpaths instead of ignoring missing supertypes.
Still open: native-image evidence covers core only (records, reflection, localized resources, and a
custom module), so decide which Spring and Kotlin paths a major version supports before claiming
them; model or stream refactoring needs comparable-machine JMH evidence against the documented
budgets rather than a smoke run.

The previous preparation plan and historical implementation documents remain available in Git
history. The retained v3 branch is planning history, not an additional release line to publish.
