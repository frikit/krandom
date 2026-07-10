# V2 Generation Recipes and Named Child Streams

**Status:** In Progress
**Master plan:** [Step 2.7](v2-master-implementation-plan.md#step-27--add-versioned-deterministic-recipes-and-child-streams)

## Contract decisions

- A portable recipe is available only for a seed-owned configuration. Caller-owned random
  instances, random factories, secure random mode, and executable object rules have state that
  cannot be represented safely, so they deliberately report no portable recipe.
- Recipe format and algorithm identifiers are independently versioned. Version `v1` uses
  `java.util.Random` plus a stable, named-child-stream derivation. Patch releases preserve both;
  a changed algorithm requires a new recipe version.
- Recipes capture a clock instant and zone, then replay with a fixed clock. This turns a
  time-relative failure into a reproducible configuration without pretending that a live clock is
  stable.
- Child identities are structural: object fields use their declared owner and field path; schema
  values use the field name and record index; list, set, array, map, and recursive values append
  an explicit index or key/value segment. A field added elsewhere must not change an existing
  identity.

## Stage 1: Define and serialize the recipe

**Goal:** Add one public, portable recipe representation and make seed-owned configurations expose
it.
**Success Criteria:** A recipe contains library/recipe/algorithm versions, seed, locale, captured
clock, profile, safety policy, construction policy, provider-dataset version, and all supported
scalar configuration settings; it has a stable human-readable encoding and recreates an equivalent
configuration.
**Tests:** Round-trip encoding, malformed/unknown recipe rejection, replay configuration equality,
and non-portable source rejection.
**Status:** Complete

### Coverage reassessment (2026-07-10)

The first recipe implementation passed its focused tests but missed the repository-wide 99.9%
coverage gate three times:

1. The first full-core run reported 99.6% instruction coverage and 98.6% branch coverage after
   introducing the recipe model.
2. Adding parser, configuration, and serialization edge cases raised that to 99.8% instructions
   and 99.2% branches, but still failed the same gate.
3. Covering portable-state alternatives raised branch coverage to 99.7%, while instruction
   coverage remained 99.8%; it still failed the required 99.9% threshold.

The failure is structural, not a reason to weaken the gate: the first design spreads parsing,
replay setting application, and portability checks across many short-circuit branches. Before a
fourth attempt, compare it with the existing compact value-object patterns (`GenerationFailureDiagnostic`
and `LocaleDataBundle`) and the existing configuration-copy pattern (`GeneratorConfig.toBuilder()`),
then reduce the recipe's branching surface. The revised design must retain strict rejection of
non-portable state and preserve the documented replay contract.

The reassessment found three useful local alternatives:

1. `GenerationFailureDiagnostic` is a narrow validated record: recipe diagnostics should keep one
   structured value rather than duplicate validation across integrations.
2. `LocaleDataBundle` keeps mutable builder concerns at its edge and exposes immutable snapshots;
   recipe settings should likewise be normalized once, not rechecked by each consumer.
3. `GeneratorConfig.toBuilder()` already defines the library's configuration-copy boundary; recipe
   replay should remain a conversion through that boundary rather than create a second object
   configuration system.

The revised approach therefore collapses portability checks into one explicit state scan and keeps
the recipe as a value object. It will add only behavior-focused tests for the remaining parser
boundaries, not test-only scaffolding for every short-circuit expression.

The full pre-commit gate now covers the portable-state alternatives, stable serialization, replay,
and malformed-boundary cases at the repository coverage threshold. The environment contract also
proves that changing locale, clock, provider-dataset version, or safety-policy label changes the
recipe identity. The typed payment-card safety policy is persisted separately as a replay setting,
so a checksum-valid validator fixture or selected processor sandbox policy cannot silently replay
as the default non-routable output.
Recipes without that new setting preserve their historic checksum-valid behavior for v1 replay
compatibility; newly emitted recipes always record the policy explicitly.
Phone-number safety follows the same rule: new recipes record the selected policy, while recipes
without `phone-number.safety-policy` retain historic realistic-unclassified phone output.
National-ID safety also follows this compatibility rule: new recipes record
`national-id.safety-policy` and default to `DISABLED`, while recipes without that setting retain
their historical realistic-unclassified replay behavior.
Banking safety follows the same rule: new recipes record `banking.safety-policy` and default to
`DISABLED`, while recipes without that setting retain their historical realistic-unclassified replay
behavior.
Identity-document safety follows the same rule: new recipes record
`identity-document.safety-policy` and default to `DISABLED`, while recipes without that setting
retain their historical realistic-unclassified replay behavior.
Business tax-identifier safety follows the same rule: new recipes record
`business-tax-identifier.safety-policy` and default to `DISABLED`, while recipes without that
setting retain their historical realistic-unclassified replay behavior.
Crypto-address safety follows the same rule: new recipes record `crypto-address.safety-policy` and
default to `DISABLED`, while recipes without that setting retain their historical
realistic-unclassified replay behavior.
Securities-identifier safety follows the same rule: new recipes record
`securities-identifier.safety-policy` and default to `DISABLED`, while recipes without that setting
retain their historical realistic-unclassified replay behavior.

## Stage 2: Derive structural child streams

**Goal:** Replace positional seeded draws at object and schema structural boundaries with named
child streams.
**Success Criteria:** Existing named fields and schema columns retain their values when unrelated
fields are added. Repeated collection/map elements and recursive objects have documented,
deterministic identities.
**Tests:** Object and schema stability fixtures, repeated-element replay, nested-object replay, and
checked-in golden streams.
**Status:** Complete

Schema now derives the stream identity `schema|record=<index>|field=<length>:<name>` for every
portable seeded field. The focused stability test proves that inserting an unrelated column before
existing columns does not change their values, and that a serialized recipe replays several record
indexes exactly.

Object members use `object|owner=<length>:<owner>|kind=<kind>|member=<length>:<member>`. The
member's child seed creates its resolver, so another field cannot consume its random values. Nested
objects receive the parent member's deterministic child seed and establish their own member streams.
Arrays, lists, sets, and maps consume their member stream in deterministic encounter order; array
and list entries use their zero-based index, sets use insertion order, and maps use each key/value
pair's insertion order. Appending a repeated value preserves existing positions; inserting one
before another intentionally changes its positional identity. Parallel calls remain memory-safe but
the ordering of calls on one mutable generator is not a replay contract—use one generator per task
or replay each task from a separate recipe.

`GenerationRecipeGoldenStreamTest` is the checked-in v1 stream baseline. It pins a representative
scalar sequence, a record fixture, and schema records. Any deliberate algorithm change must add a
new recipe version and a new baseline rather than silently change this stream.

## Stage 3: Surface replay safely

**Goal:** Attach safe recipes to failure diagnostics and framework integrations, and document the
compatibility promise.
**Success Criteria:** Failures expose one copyable recipe when the configuration is portable;
non-portable configurations say why without exposing values or source internals.
**Tests:** Object/schema failure diagnostics, JUnit integration coverage, compatibility-policy
tests, and published consumer examples.
**Status:** In Progress

Object-generation listeners now receive `GenerationFailureDiagnostic.replayIdentity` when the
source configuration is portable. JUnit publishes the safe recipe under `krandom.recipe` and prints
it with the failure seed. Diagnostic recipes keep the derived numeric seed but omit `seed-text`, so
user-provided textual seed material is not logged. Schema generation failures expose and print the
same safe recipe. The remaining host-framework integrations are tracked in their dedicated Stage 3
packages.
