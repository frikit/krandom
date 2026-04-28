# Codex Java Library Parity 100 Plan — 2026-04-28

## Goal

Make krandom defensibly **100% complete for its scoped Java generator-library parity contract**.

This does not mean cloning every method, novelty catalog, or implementation detail from every Java fake-data library. It means:

- Core generator capabilities are present and easy to use with US defaults.
- Java object-generation capabilities cover the useful Easy Random/Instancio-style fixture workflows.
- Every remaining competitor feature is either implemented, intentionally excluded with rationale, or promoted to a concrete implementation task.

## Sources Checked

- DataFaker provider docs: https://www.datafaker.net/documentation/providers/
- JavaFaker README: https://github.com/DiUS/java-faker
- Easy Random README and configuration docs: https://github.com/j-easy/easy-random and https://github.com/j-easy/easy-random/wiki/Randomization-parameters
- Instancio user guide: https://www.instancio.org/user-guide/
- Local source and plans under `core/src/main/java`, `docs/feature-parity`, and `docs/plans`.

## Current Verdict

The scoped Java parity target is now clean:

| Area | Verdict | Rationale |
|------|---------|-----------|
| DataFaker / JavaFaker core providers | 100% scoped | Identity, address, networking, finance, commerce, company, job, text, date/time, phone, codes, color, templates, uniqueness, schema output, custom providers, and object population are implemented or explicitly scoped. |
| DataFaker / JavaFaker long-tail catalogs | 100% classified | Entertainment, sports, food, animals, medical, vehicle, aviation, and stock catalogs are intentional non-goals unless promoted by product demand. |
| Easy Random object generation | 100% scoped | POJO/record generation, Objenesis fallback, recursion guard, depth, overrides, exclusions, declarative randomizers, common Java types, Bean Validation basics, and realistic data are implemented. |
| Easy Random implementation-specific SPI | 100% classified | `java.util.Random` subclassing, ServiceLoader registries, registry priority, object factory hooks, and classpath scanning are intentional non-goals. |
| Instancio parity | Not claimed as API clone | krandom overlaps with object graph generation, selectors via object/config APIs, null handling, and Java type coverage, but does not clone Instancio's full selector/model/fill/JUnit ecosystem. That is a separate product line if needed. |

## Actions Completed In This Sweep

- Updated `docs/feature-parity/datafaker-parity.md` with a Java parity contract and removed ambiguous low-priority long-tail rows.
- Marked stale DataFaker rows as implemented: HTTP method, commerce department/material, company buzzword/catchphrase/BS phrase, phone international/MSISDN support, digit-not-zero via ranges, MD5/SHA digest support, XML schema output, custom providers, and object population.
- Updated `docs/feature-parity/easy-random-parity.md` with a Java object-generation parity contract.
- Marked stale Easy Random rows as implemented: object streams, simple generics, type exclusions, randomizer context, basic Bean Validation non-null/non-empty behavior, URI strings, legacy date range, collection/map population, optional behavior, password generation, and queue support.
- Marked Easy Random implementation-specific rows as intentional skips: `Random` subclassing, classpath scanning, `ObjectFactory`, `ExclusionPolicy`, registry/provider SPI, ServiceLoader discovery, specialized legacy maps, null/skip randomizer classes.
- Marked `docs/plans/datafaker-java-plan.md`, `docs/plans/easy-random-java-plan.md`, and `docs/plans/parity-gap-plan-v3-2026-04-28.md` as completed for the scoped Java parity sweep.

## Remaining Product Decisions

These are not open bugs; they are scope decisions.

1. Instancio API clone: only pursue if krandom should compete directly as an object-fixture framework, not just a generator library. Required capabilities would be model templates, selector precedence/scopes/depth, fill existing objects, assignment/filter callbacks, JUnit extension parity, and Kotlin selector ergonomics.
2. DataFaker long-tail catalogs: only add as separate optional modules if users request maintained domain packages, for example `krandom-food`, `krandom-sports`, or `krandom-vehicles`.
3. YAML compatibility: only add DataFaker YAML path/URL loading or expression syntax if external YAML datasets become a target integration format.
4. Runtime classpath scanning: keep rejected by default because it adds startup cost, nondeterminism, and dependency surface. Revisit only for an opt-in object-fixture module.

## Maintenance Gate

Before claiming Java-library parity is still 100%:

1. Check official DataFaker, JavaFaker, Easy Random, and Instancio docs for changed surfaces.
2. Search local parity docs for ambiguous active-gap wording or plain active `No` rows in Java-library documents.
3. Source-verify every supposed gap with `rg` before adding code work.
4. Either implement the feature with tests or mark it `No (intentional)` / `SKIP` with rationale.
5. Run `./scripts/pre_commit_check.sh` before finishing.

## Commit Scope

This sweep is documentation and planning only. No production source code was changed because the reviewed Java-library gaps were already implemented or intentionally out of scope.
