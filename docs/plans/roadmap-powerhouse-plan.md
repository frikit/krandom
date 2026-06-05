# krandom Powerhouse Roadmap

## Scope

- Goal: transform krandom from a strong internal library into a production-grade, widely adopted JVM data-generation toolkit.
- Organized in four tiers by impact. Tiers are sequential — complete Tier 1 before starting Tier 2.
- Each item has a checkbox for tracking.

## Delivery Rules

- Implement in vertical slices (one item at a time).
- For each slice:
    1. Implement changes.
    2. Add/update tests.
    3. Run `./scripts/pre_commit_check.sh`.
    4. Update this checklist.
- Pre-commit must pass and coverage must not regress.

---

## Tier 1: Distribution & Credibility

Goal: remove all adoption blockers. Nothing else matters until users can `implementation("io.github.frikit:krandom-core:1.0.0")` from Maven Central.

- [x] **1.1 Align Java package namespace** — rename `io.github.frikit.krandom` to `io.github.frikit.krandom` across all modules, tests, examples, and docs. Required for Sonatype namespace verification.
- [x] **1.2 Publish to Maven Central** — register `io.github.frikit` on Sonatype OSSRH, add GPG signing to the build, create a `release-maven-central.yml` workflow. (Build config + workflow ready; user must register on OSSRH and configure secrets.)
- [x] **1.3 CHANGELOG.md** — create changelog covering all notable changes since project inception. Adopt keep-a-changelog format.
- [ ] **1.4 Community files** — add `CONTRIBUTING.md` (fork/PR workflow, 99.9% coverage gate, pre-commit usage, Java 21 requirement), `CODE_OF_CONDUCT.md` (Contributor Covenant), `SECURITY.md` (vulnerability reporting via GitHub security advisories).
- [x] **1.5 Release v1.0.0** — tag, build, and publish to Maven Central. Update README install instructions to remove GitHub Packages credentials requirement. (README updated; user triggers the Maven Central workflow after configuring OSSRH secrets.)

---

## Tier 2: API & Safety

Goal: polish the public API for discoverability and correctness in real-world usage.

- [x] **2.1 Fluent domain namespaces** — add `Generators.person()`, `Generators.finance()`, `Generators.location()`, `Generators.network()`, `Generators.text()`, `Generators.commerce()`, `Generators.identifier()`, `Generators.datetime()` returning typed namespace objects. Keep existing `ofXxx()` methods as-is for backwards compatibility.
- [x] **2.2 Thread safety documentation** — add `@apiNote` Javadoc on `Generator<T>` and `Generators` clarifying that instances are not thread-safe. Add `Generators.threadLocal(Generator<T>)` wrapper that returns a `ThreadLocal`-backed generator.
- [x] **2.3 Optimize ObjectGenerator hot path** — cache settable-field and record-component metadata per `Class<T>` with `ClassValue`. Reduces per-call reflection overhead without pinning class keys in a global map.
- [x] **2.4 Replace reflection-based reseed** — introduce a `Seedable` interface with `void reseed(long seed)`. Implement on `AbstractBoundedGenerator` and all stateful generators. Keep the reflection fallback on the `Generator` default method for custom lambdas but deprecate reliance on it.

---

## Tier 3: Ecosystem Integration

Goal: make krandom the natural choice for JVM test infrastructure.

- [x] **3.1 Kotest Arb adapter** — new module `kotest-extensions` bridging `Generator<T>` to Kotest `Arb<T>` for property-based testing.
- [x] **3.2 jqwik Arbitrary adapter** — new module `jqwik-extensions` bridging `Generator<T>` to jqwik `Arbitrary<T>`.
- [x] **3.3 Annotation-driven generation** — `@Fake("email")`, `@FakeRange(min=1, max=100)` on fields as declarative alternative to `ObjectFaker.ruleFor()`. Process annotations in `ObjectGenerator`.
- [x] **3.4 Spring Boot test slice** — `@KrandomTest` annotation in `spring-boot-starter` that loads only krandom auto-configuration beans. Enables lightweight test fixtures without full application context.

---

## Tier 4: Features & Scale

Goal: differentiate krandom with capabilities no competitor offers.

- [x] **4.1 OpenAPI/JSON Schema input** — new module or core feature: parse an OpenAPI spec or JSON Schema and produce `Schema` instances with field-appropriate generators. No competitor does this well.
- [x] **4.2 Streaming bulk export** — add `Schema.writeTo(OutputStream, format, count)` variants that write directly to output streams with bounded memory. Enable million-row dataset generation without OOM.
- [x] **4.3 Kotlin DSL module** — new module `kotlin-dsl` providing `krandom { person { firstName = fixed("Ada") } }` builder syntax.
- [x] **4.4 Expand locale coverage** — grow from 20 to 50+ locales via community PRs. Leverage the existing quality-tier system and `locale-contribution-guide.md`.
- [ ] ~~**4.5 Database seeding module**~~ — skipped per user decision.
- [x] **4.6 Published benchmark dashboard** — add benchmark result tables to the docs-site. Automate monthly runs via scheduled GitHub Action.
