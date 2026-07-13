# v2 Spring Contract Completion Plan

**Created:** 2026-07-13
**Status:** Complete
**Parent work package:** [Master plan Step 3.5](v2-master-implementation-plan.md#step-35--make-krandomtest-a-real-spring-test-slice)

## Stage 1: Characterize the published property contract

**Goal**: Identify every bindable `krandom.*` property and the core value that supplies its
default.

**Success Criteria**: The generated Spring metadata is treated as a published artifact, not an
implementation detail; all properties, descriptions, and core defaults have one testable source.

**Tests**: A focused test reads both metadata resources packaged by the starter and compares their
merged `krandom.*` entries with `GeneratorConfig.defaults()`.

**Status**: Complete

## Stage 2: Complete metadata and default alignment

**Goal**: Document every supported property with an accurate type, description, and fixed default
where the core exposes one.

**Success Criteria**: Recipe/clock settings and all construction/safety policies appear in Spring
metadata; policy defaults match the core exactly; optional inputs do not claim invented defaults.

**Tests**: The metadata contract fails for missing, undocumented, or default-drifted properties.

**Status**: Complete

## Stage 3: Prove slice and full-context binding equivalence

**Goal**: Verify both supported Spring test modes produce the documented `GeneratorConfig`.

**Success Criteria**: `@KrandomTest` remains isolated, while a real `@SpringBootTest` binds the
same seed, locale, object, collection, clock, and safety-policy contract.

**Tests**: Focused slice tests plus an explicit full-context test using the supported Spring Boot
line.

**Status**: Complete

## Stage 4: Verify the published starter path

**Goal**: Validate the finished starter through its full module and locally published consumer
examples.

**Success Criteria**: Module tests and Gradle/Maven integration examples pass against the local
`2.0.0-SNAPSHOT` artifact; master-plan and backlog statuses accurately record the evidence.

**Tests**: `:spring-boot-starter:test`, `./scripts/verify_examples_local.sh`, and the full
pre-commit gate.

**Status**: Complete
