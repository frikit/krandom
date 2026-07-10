# V2 Bean Validation Normalization

**Status:** Implementation in progress; Stages 1–3 complete
**Scope:** Jakarta Validation annotations consumed by Java object generation

## Goal

Object generation must either return a value satisfying every supported constraint on that value or
fail before returning the fixture. Constraint annotations are inputs to one normalized contract, not
an ordered list of generators where the first recognized annotation wins.

## Baseline behavior and gaps

`BeanValidationSupport` currently recognizes assertion, null, string, numeric, sign, and temporal
annotations and already has an end-to-end Hibernate Validator smoke test. The implementation derives
a generator directly from reflection each time, which leaves these correctness gaps:

| Area | Baseline behavior | Required v2 behavior |
|---|---|---|
| Nullability | `@Null` is supported; `@NotNull` and `@NotEmpty` are not | Normalize null/required/empty rules and reject contradictions |
| Size | `@Size(min > max)` silently raises `max` to `min` | Reject impossible ranges before generation |
| Numeric bounds | Inverted min/max silently collapse to the lower bound | Intersect integral, decimal, and sign bounds; reject an empty interval |
| Assertions | `@AssertTrue` wins when true and false are both present | Reject contradictory assertions |
| Time | Future is checked before past | Intersect temporal direction/presence and reject contradictions |
| Text | Email, pattern, numeric text, size, and not-blank use first-match precedence | Generate from the intersection and validate the result |
| Null probability | Any existing BV generator suppresses random nulls, but required-only annotations do not | Required constraints always suppress null/empty probability |
| Containers | `@Size` is shared by arrays and collections | Add `@NotEmpty`; keep one validated size range for every supported container |
| Diagnostics | Invalid intersections are converted into plausible-looking values | Fail with the complete field path, declared type, and constraint reason |

## Precedence contract

1. Explicit contextual/configured field and type overrides remain authoritative extension points.
2. Declarative custom generators (`@Randomizer`, `@Fake`, `@FakeRange`) remain explicit generation
   rules.
3. Bean Validation annotations constrain default generation and null/empty probability.
4. Semantic-name and built-in generators are defaults and must not bypass a recognized constraint.
5. Explicit custom outputs are caller-owned until Stage 4 decides whether opt-in post-validation is
   practical without breaking existing extension contracts.

This ordering preserves the current override API while removing accidental precedence between
default semantic generation and supported validation annotations.

## Normalized model

The package-private model must contain only facts needed for generation:

- nullability: required, null-only, non-empty, non-blank;
- size interval;
- numeric interval with inclusive/exclusive endpoints;
- optional boolean assertion;
- temporal direction and whether the present instant is allowed;
- text pattern/email requirements;
- a stable list of contributing annotation names for diagnostics.

Normalization performs all intersections once. It never repairs an empty intersection by widening or
collapsing it.

## Stage 1: Nullability, size, and precedence

**Goal:** Make required/empty behavior explicit for strings, arrays, collections, maps, and optionals.

**Success Criteria:**

- `@NotNull` always suppresses null probability.
- `@NotEmpty` implies required plus minimum size one.
- `@NotBlank` implies required and a non-whitespace string.
- `@Null` combined with a required constraint fails before field generation.
- `@Size(min > max)` fails instead of silently changing the annotation contract.
- Recognized constraints take precedence over default semantic-name generation.

**Tests:** Focused field/record tests, strict/lenient diagnostics, and Hibernate Validator checks over
deterministic seeds.

**Status:** Complete

`ConstraintModel` now normalizes null-only, required, non-empty, non-blank, and size facts once per
field or record component. Required constraints suppress null/empty probability; `@NotNull` on an
Optional keeps the Optional object non-null without requiring a contained value. Invalid targets,
`@Null`/required conflicts, primitive `@Null`, inverted sizes, and `@NotEmpty` intersected with
`@Size(max = 0)` fail through structured generation context. Recognized validation constraints also
take precedence over default strict semantic-name generation.

## Stage 2: Numeric, assertion, and temporal intersections

**Goal:** Normalize compatible bounds and reject impossible scalar contracts.

**Success Criteria:**

- Integral, decimal, inclusive/exclusive, and sign annotations intersect exactly.
- Integral target domains are considered when deciding whether an interval is empty.
- Conflicting boolean assertions and future/past annotations fail before generation.
- Generated values validate against the configured clock.

**Tests:** Every supported scalar type, exact endpoints, exclusive endpoints, contradictory pairs,
and deterministic multi-seed Hibernate validation.

**Status:** Complete

Numeric annotations now normalize into inclusive/exclusive decimal endpoints before being projected
onto integral, arbitrary-precision, floating-point, or numeric-string target domains. Empty real
intervals, integral gaps, floating-point representability gaps, malformed decimal bounds, and
unsupported targets fail contextually instead of collapsing to a plausible value. Boolean
assertions normalize to one value or fail on contradiction. Temporal annotations intersect past,
present, and future domains; present-only intersections use the configured clock for every
supported target, while strict `LocalTime` and `MonthDay` clock-boundary failures follow the same
strict/lenient structured policy. A deterministic 32-seed fixture matrix is verified by Hibernate
Validator using the identical fixed clock.

## Stage 3: Text composition

**Goal:** Make email, regex, size, not-empty, and not-blank constraints composable.

**Success Criteria:**

- A generated string satisfies every recognized text constraint.
- Unsupported regex features or an exhausted bounded search fail contextually.
- Email generation respects compatible size and pattern constraints.
- Repeatable patterns are intersected rather than ignored.

**Tests:** Compatible and incompatible email/pattern/size compositions, repeatable patterns, numeric
strings, and record/accessor annotations.

**Status:** Complete

String constraints now normalize email, every repeatable `@Pattern`, numeric bounds, size, and
blankness into one contract. Candidate generation rotates through pattern, email, numeric, and sized
text sources, accepting only a value that satisfies the complete contract. Search is capped at 256
candidates so incompatible compositions fail deterministically through structured strict/lenient
handling. Java regex flags are honored during validation; malformed Java expressions and valid
expressions outside `RegexGenerator`'s supported generation syntax fail contextually. Tests cover
compatible and incompatible compositions, custom email regexes, record components, JavaBean
getters, and interface accessor annotations across deterministic seeds.

## Stage 4: Validation matrix and published support table

**Goal:** Prove and document the complete advertised contract from code facts.

**Success Criteria:**

- Hibernate Validator accepts generated fixtures over a deterministic seed matrix.
- The support table names target types, composition behavior, and known exclusions.
- Unsupported annotations remain ordinary metadata and are not falsely advertised.
- Master-plan checkboxes and changelog entries match verified implementation facts.

**Tests:** Full Java 21 pre-commit gate plus a deterministic validator matrix covering fields,
getters, inherited accessors, and Java record components.

**Status:** Not Started

## Completion gate

- No supported contradiction is silently repaired.
- Null and empty probability cannot violate a required constraint.
- Supported generated values pass Hibernate Validator.
- Failures use the structured generation policy and disclose no generated values.
- Public APIs remain compatible during the 1.6 bridge.
