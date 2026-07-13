---
layout: page
title: Bean Validation Support
permalink: /guides/bean-validation/
---

# Bean Validation Support

`ObjectGenerator` natively reads 21 Jakarta Validation constraints from fields, JavaBean getters,
record components, and interface accessor declarations. Supported constraints form one contract:
compatible annotations are intersected, and an impossible contract fails before a fixture is
returned.

## Support matrix

| Constraint | Generated targets | Composition and failure behavior |
| --- | --- | --- |
| `@AssertFalse` | `boolean`, `Boolean` | Intersects with `@AssertTrue`; contradictory assertions fail. |
| `@AssertTrue` | `boolean`, `Boolean` | Intersects with `@AssertFalse`; contradictory assertions fail. |
| `@DecimalMax` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Inclusive/exclusive decimal endpoints intersect with every numeric and sign bound; empty real or target domains fail. |
| `@DecimalMin` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Inclusive/exclusive decimal endpoints intersect with every numeric and sign bound; malformed bounds and empty domains fail. |
| `@Email` | `String` | The email shape, custom email regex, repeatable patterns, numeric bounds, size, and blankness are composed through bounded search. |
| `@Future` | Supported Java temporal types listed below | Intersects with present/past constraints using `GeneratorConfig.getClock()`; an empty direction fails. |
| `@FutureOrPresent` | Supported Java temporal types listed below | Can intersect with `@PastOrPresent` at the configured present. |
| `@Max` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Intersects with decimal, minimum, and sign bounds; integral and floating target domains are checked. |
| `@Min` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Intersects with decimal, maximum, and sign bounds; integral and floating target domains are checked. |
| `@Negative` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Adds an exclusive upper bound at zero and intersects with all other numeric bounds. |
| `@NegativeOrZero` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Adds an inclusive upper bound at zero and intersects with all other numeric bounds. |
| `@NotBlank` | `String` | Implies non-null and minimum size one; composes with email, repeatable patterns, numeric text, and size. |
| `@NotEmpty` | `String`, arrays, `Collection` implementations, `Map` implementations | Implies non-null and minimum size one; an empty size intersection fails. |
| `@NotNull` | Reference fields, including the `Optional` object | Suppresses null probability. It does not require an `Optional` to contain a value. |
| `@Null` | Reference fields | Forces `null`; primitive and required intersections fail. |
| `@Past` | Supported Java temporal types listed below | Intersects with present/future constraints using the configured clock; an empty direction fails. |
| `@PastOrPresent` | Supported Java temporal types listed below | Can intersect with `@FutureOrPresent` at the configured present. |
| `@Pattern` | `String` | Repeatable patterns and Java regex flags are all enforced with email, numeric, size, and blankness rules. |
| `@Positive` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Adds an exclusive lower bound at zero and intersects with all other numeric bounds. |
| `@PositiveOrZero` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Adds an inclusive lower bound at zero and intersects with all other numeric bounds. |
| `@Size` | `String`, arrays, `Collection` implementations, `Map` implementations | Intersects with `@NotEmpty`/`@NotBlank`; inverted and empty ranges fail. |

Supported temporal targets are `Instant`, `LocalDate`, `LocalDateTime`, `ZonedDateTime`,
`OffsetDateTime`, `Date`, `java.sql.Date`, `Timestamp`, `Calendar`, `LocalTime`, `OffsetTime`,
`Year`, `YearMonth`, and `MonthDay`.

## Precedence

Explicit contextual/configured overrides and declarative custom generators remain authoritative
extension points. Bean Validation constrains default generation and null/empty probability. It wins
over semantic field-name and built-in defaults, so a semantic name cannot bypass a supported
constraint. Outputs from explicit custom rules remain caller-owned and are not post-validated.

## Bounded text composition

String composition rotates through pattern, email, numeric, and sized-text candidate sources and
accepts only a value satisfying the complete annotation contract. Search stops after 256 candidates.
Malformed Java regexes, regex syntax outside `RegexGenerator`'s supported generation subset,
incompatible repeatable patterns, and exhausted searches use the same structured strict/lenient
failure policy as other object-generation errors.

## Honest exclusions

- Constraints not listed above, such as `@Digits`, remain ordinary metadata; kRandom does not claim
  to enforce them.
- A listed constraint on an unsupported target fails contextually instead of being ignored.
- Explicit custom generator and override outputs are not silently changed or post-validated.
- Strict `LocalTime` and `MonthDay` constraints can have no representable value at a configured clock
  boundary; those cases fail rather than wrapping into the wrong temporal direction.
