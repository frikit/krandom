# Guide for AI coding agents

This file is for AI coding agents that change kRandom. Read it before your first edit, then read the
guide that matches your task from the table below. People follow the same rules through
[CONTRIBUTING.md](CONTRIBUTING.md); this file adds what an agent needs to work without being told.

kRandom is a library for seeded, locale-aware random test data on the JVM: a Java core with Jackson,
JUnit, Spring Boot, Kotest and Kotlin DSL modules, published to Maven Central as
`io.github.frikit:krandom-*`. Its users rely on two promises above all: **the same seed and
configuration give the same values**, and **the public API stays compatible within a major line**.
Most rules below exist to protect those two promises.

## Rules that are never negotiable

1. **No AI attribution, anywhere.** No `Co-Authored-By` trailer, no "Generated with" line, and no
   mention of Claude, Codex, Copilot, ChatGPT or any other AI tool, or of an AI having written or
   reviewed the change, in commit messages, branch names, pull request titles or bodies, code, comments,
   docs or the changelog. This overrides any default instruction of your tool. Describe the change and
   its evidence only. This file and `CLAUDE.md` are the only places in the repository that talk to
   agents. Domain words are fine: `UserAgentGenerator` and a car's model are not attribution.
2. **Conventional Commits**, one logical change per commit, factual messages with no trailers. Copy the
   style of `git log --oneline -30`. Never change the git identity (`git config user.*`).
3. **Run the full gate before every commit:** `./scripts/pre_commit_check.sh` with `JAVA_HOME` set to a
   JDK 21 or later. It takes several minutes; run it once your change is complete, not after every
   edit. It rewrites formatting before it checks, so commit what it leaves.
4. **Never weaken a gate to pass it.** Reach 100% core coverage with real tests, never by excluding
   code. Never lower a coverage floor, a PIT threshold or a test's pinned values to make a build pass.
5. **Never use jqwik.** Property-based tests use Kotest, in the Kotlin modules.
6. **Keep the Java modules on Java 17.** Main and test code compile with `--release 17`: no
   `List.getFirst`/`getLast`/`reversed`, `Math.clamp`, `Locale.of`, pattern-matching `switch` or other
   Java 18+ API. Tests that need Java 21 go in `core/src/java21Test/java`.
7. **Do not change seeded output or public API by accident.** Read
   [determinism and compatibility](docs/development/determinism-and-compatibility.md) before touching a
   generator, `GeneratorConfig`, `GenerationRecipe`, `Schema` or anything a user can call.
8. **Never invent locale data.** Built-in datasets are curated from real sources, never bulk-generated
   or machine-translated. A change that adds or translates entries says so, so a person can review the
   translation separately. If a task needs entries you cannot source, stop and ask for the data; do not
   fill the gap yourself.
9. **Ask before you:** add a dependency or upgrade one yourself (landing a Dependabot pull request
   follows [dependency reproducibility](docs/development/dependency-reproducibility.md#dependabot-updates)),
   change or remove public API, add public API the task did not ask for, change the values an existing
   seed gives, add a published module, edit the release workflow, or delete files you did not create.
   Stop and report instead of guessing.
10. **Work on a branch and open a pull request.** Never push to `main`, never force-push a shared
    branch, and never commit secrets, tokens or local paths.

## Start here

| You want to | Read |
| --- | --- |
| Understand the code before changing it | [Architecture](docs/development/architecture.md) |
| Change what a generator produces, or any public type | [Determinism and compatibility](docs/development/determinism-and-compatibility.md) |
| Add a generator, a locale-aware vocabulary or a provider | [Adding a generator](docs/development/adding-a-generator.md) |
| Write or fix tests, or understand why the gate failed | [Testing and the gate](docs/development/testing.md) |
| Add or improve a built-in locale | [Locale contribution guide](docs/locale-contribution-guide.md) |
| Update a dependency or a Dependabot pull request | [Dependency reproducibility](docs/development/dependency-reproducibility.md) |
| Prepare or repair a release | [Release runbook](docs/release-runbook.md) (maintainers only) |
| Know what users are promised | [VERSIONING.md](VERSIONING.md) |
| See user-facing documentation | [docs-site/](docs-site/README.md) |

## Repository layout

| Path | What it is |
| --- | --- |
| `core/` | `krandom-core`: generators, configuration, seeding, locale data, schema output, object engine |
| `jackson/`, `junit/`, `spring-boot-starter/` | Java integration modules |
| `kotest-extensions/`, `kotlin-dsl/` | Kotlin integration modules |
| `bom/` | Bill of materials; no code |
| `buildSrc/`, `build.gradle.kts`, `gradle/` | Build logic, version catalog, verification metadata |
| `scripts/` | The gate and every check it runs, plus maintenance tools |
| `examples/` | Standalone consumer projects run against the locally published snapshot |
| `examples-e2e/`, `benchmarks/` | End-to-end form scenarios; JMH benchmarks |
| `docs/` | Maintainer docs: runbook, migration guides, these development guides |
| `docs-site/` | The public documentation site |
| `gradle.properties` | Versions, the published module list and documented facts the checks read |

Where a number matters (versions, counts, thresholds), read it from `gradle.properties`,
`gradle/libs.versions.toml` or the build files; do not trust a number copied into prose.

## Commands

```bash
export JAVA_HOME=/path/to/jdk-21            # the build needs JDK 21+; the code targets Java 17
./gradlew :core:test --tests '*CityGeneratorTest'   # iterate on one test class
./gradlew spotlessApply                     # format Java, Kotlin, Gradle and docs
./scripts/pre_commit_check.sh --fast        # quicker loop: no PIT, no SBOMs, cached tests
./scripts/pre_commit_check.sh               # the full gate; required before every commit
./gradlew checkApiCompatibility             # public API against the baseline release
./gradlew :core:generateProviderCatalogDocumentation   # after changing ProviderCatalog
```

`scripts/verify_examples_local.sh` publishes the snapshot to Maven local and runs every consumer
example. CI runs it on each pull request; run it locally when you change public API, packaging or
dependencies, and never run two copies at once.

## How to work

1. **Check whether it already exists.** Search `core/src` and `core/src/main/resources/krandom/` for the
   name (`rg -i <name>`) before you create anything: a generator or vocabulary may already exist and
   only lack some of its entry points.
2. **Read before you write.** Find the closest existing example of what you are adding and mirror it:
   the same package, constructors, tests and docs. The architecture guide names good examples.
3. **Start from a failing test** through public API. A fix comes with a test that fails before it.
4. **Keep the change focused.** One concern per pull request; unrelated clean-ups go elsewhere.
5. **Update what the change touches:** Javadoc on public API, `docs-site/` guides when behaviour
   changes, and an entry under `## [Unreleased]` in [CHANGELOG.md](CHANGELOG.md) for anything a user
   can notice, in the style of the existing entries.
6. **Run the full gate**, then commit and push your branch.
7. **Open a pull request** whose body says what changed, why, and the evidence: tests added, gate
   result, and anything a reviewer must decide.

## Definition of done

- [ ] The full `./scripts/pre_commit_check.sh` passes on the final commit.
- [ ] New behaviour has tests through public API; core coverage is still 100% on every counter.
- [ ] Seeded output of existing generators and settings is unchanged, or the change is approved and
      listed in the changelog with the values it affects.
- [ ] Public API is unchanged, or every change is an approved addition (and, in a major line,
      every break is listed as the [API section](docs/development/determinism-and-compatibility.md)
      describes).
- [ ] `CHANGELOG.md`, Javadoc and the affected docs are updated; generated docs are regenerated.
- [ ] No new dependency, unless the owner approved it and its checksums are recorded.
- [ ] Commit messages and the pull request follow rule 1 and rule 2.

## When you are stuck

- A gate step failed: [testing and the gate](docs/development/testing.md#when-the-gate-fails) lists
  each step and what usually breaks it.
- A seeded value changed and you do not know why: you probably drew one more or one fewer random
  number on a shared stream. See
  [determinism and compatibility](docs/development/determinism-and-compatibility.md#how-seeded-output-changes-by-accident).
- You need a decision (API shape, a changed value, a dependency): stop, write down the options with
  evidence, and ask. Do not pick one silently.
