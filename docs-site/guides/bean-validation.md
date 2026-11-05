---
layout: page
title: Bean Validation Support
permalink: /guides/bean-validation/
---

# Bean Validation Support

`ObjectGenerator` natively reads 21 Jakarta Validation constraints from fields, JavaBean getters,
record components, interface accessor declarations, and constructor parameters. A constructor
argument also meets the constraints of the field it initializes, so a constraint on a final field
reaches the value that fills it: see the
[construction policy]({{ '/guides/object-generation/' | relative_url }}#construction-policy).
Supported constraints form one contract: compatible annotations are intersected, and an impossible
contract fails before a fixture is returned.

## Dependency

The Jakarta Validation API is a compile-only dependency of `krandom-core`, so it is not pulled in
transitively. Add `jakarta.validation:jakarta.validation-api` to your own dependencies (test scope
is enough) to activate constraint-aware generation. Without the API on the class path, kRandom
detects its absence once and generates plain values; constraint annotations are ignored rather
than failing with a `NoClassDefFoundError`.

On the module path the core descriptor declares `requires static jakarta.validation`, which does
not resolve the module for you: add `requires jakarta.validation;` to your own module descriptor
(or start the JVM with `--add-modules jakarta.validation`) so the constraints are visible at run
time.

## Support matrix

| Constraint | Generated targets | Composition and failure behavior |
| --- | --- | --- |
| `@AssertFalse` | `boolean`, `Boolean` | Intersects with `@AssertTrue`; contradictory assertions fail. |
| `@AssertTrue` | `boolean`, `Boolean` | Intersects with `@AssertFalse`; contradictory assertions fail. |
| `@DecimalMax` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Inclusive/exclusive decimal endpoints intersect with every numeric and sign bound; empty real or target domains fail. |
| `@DecimalMin` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Inclusive/exclusive decimal endpoints intersect with every numeric and sign bound; malformed bounds and empty domains fail. |
| `@Email` | `String` | The email shape, custom email regex, repeatable patterns, numeric bounds, size, and blankness are composed through bounded search. Generated addresses follow the [email-domain contract]({{ '/guides/data-validity-and-safety/' | relative_url }}#email-domain-contract). |
| `@Future` | Supported Java temporal types listed below | Intersects with present/past constraints using `GeneratorConfig.getClock()`; an empty direction fails. |
| `@FutureOrPresent` | Supported Java temporal types listed below | Can intersect with `@PastOrPresent` at the configured present. |
| `@Max` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Intersects with decimal, minimum, and sign bounds; integral and floating target domains are checked. |
| `@Min` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Intersects with decimal, maximum, and sign bounds; integral and floating target domains are checked. |
| `@Negative` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Adds an exclusive upper bound at zero and intersects with all other numeric bounds. |
| `@NegativeOrZero` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Adds an inclusive upper bound at zero and intersects with all other numeric bounds. |
| `@NotBlank` | `String` | Implies non-null and minimum size one; composes with email, repeatable patterns, numeric text, and size. |
| `@NotEmpty` | `String`, arrays, `Collection` implementations, `Map` implementations | Implies non-null and minimum size one; an empty size intersection fails. A collection, array, or map takes its [size](#arrays-and-collection-sizes) from `collectionSize`. |
| `@NotNull` | Reference fields, including the `Optional` object | Suppresses null probability. It does not require an `Optional` to contain a value. A nested object member at the [depth limit]({{ '/guides/object-generation/' | relative_url }}#depth-limit) fails rather than being left `null`. |
| `@Null` | Reference fields | Forces `null`; primitive and required intersections fail. |
| `@Past` | Supported Java temporal types listed below | Intersects with present/future constraints using the configured clock; an empty direction fails. |
| `@PastOrPresent` | Supported Java temporal types listed below | Can intersect with `@FutureOrPresent` at the configured present. |
| `@Pattern` | `String` | Repeatable patterns and Java regex flags are all enforced with email, numeric, size, and blankness rules. |
| `@Positive` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Adds an exclusive lower bound at zero and intersects with all other numeric bounds. |
| `@PositiveOrZero` | Numeric primitives/wrappers, `Number`, `BigInteger`, `BigDecimal`, numeric `String` | Adds an inclusive lower bound at zero and intersects with all other numeric bounds. |
| `@Size` | `String`, arrays, `Collection` implementations, `Map` implementations | Intersects with `@NotEmpty`/`@NotBlank`; inverted and empty ranges fail. A collection, array, or map takes its [size](#arrays-and-collection-sizes) from `collectionSize` within these bounds. |

Supported temporal targets are `Instant`, `LocalDate`, `LocalDateTime`, `ZonedDateTime`,
`OffsetDateTime`, `Date`, `java.sql.Date`, `Timestamp`, `Calendar`, `GregorianCalendar`, `LocalTime`,
`OffsetTime`, `Year`, `YearMonth`, and `MonthDay`. A `Calendar` or `GregorianCalendar` value is a
non-lenient `GregorianCalendar` with ISO week rules in the configured clock's zone, whatever the JVM's
default locale.

## Arrays and collection sizes

`@Size` and `@NotEmpty` bound the size of a collection, array, or map, and `collectionSize` chooses
the size within those bounds. With the default `collectionSize(1, 10)`, `@NotEmpty List<String>` has
1 to 10 elements, and `@Size(max = 3)` 1 to 3. Where the configured range lies outside the bounds,
the size is the bound nearest it: `@Size(min = 20)` gives 20 elements under the default, and
`@Size(max = 2)` with `collectionSize(4, 6)` gives 2.

A constraint written before an array type, as in `@NotEmpty Leaf[]` or `@Size(max = 3) String[]`,
constrains the array and not its elements. Java places such an annotation on the array's component
type as well as on the declaration, but Hibernate Validator reads it for the array alone, and so does
kRandom: `@Size(max = 3) String[]` holds up to three strings of the configured length. A constraint
on a type argument inside the component still applies, so each list in `List<@Email String>[]` holds
email addresses.

## Precedence

Explicit contextual/configured overrides and declarative custom generators remain authoritative
extension points. Bean Validation constrains default generation and null/empty probability. It wins
over semantic field-name and built-in defaults, so a semantic name cannot bypass a supported
constraint. Outputs from explicit custom rules remain caller-owned and are not post-validated.

## Bounded text composition

String composition rotates through pattern, email, numeric, and sized-text candidate sources and
accepts only a value satisfying the complete annotation contract. Search stops after 256 candidates.
A pattern outside [`RegexGenerator`'s supported syntax]({{ '/guides/data-validity-and-safety/' | relative_url }}#regular-expression-contract),
such as one with a lookahead, is not a source, but it is still enforced on every candidate, so another
source can meet it. If no source is left, or the search runs out, the failure names the unsupported
construct. Malformed Java regexes, incompatible repeatable patterns, and exhausted searches use the
same structured strict/lenient failure policy as other object-generation errors. A `@Size` minimum
lets a pattern's `*`, `+`, and `{n,}` reach that length, so `[0-9]+` with `@Size(min = 20)` works.

## Honest exclusions

- Constraints not listed above, such as `@Digits`, remain ordinary metadata; kRandom does not claim
  to enforce them.
- A listed constraint on an unsupported target fails contextually instead of being ignored.
- Explicit custom generator and override outputs are not silently changed or post-validated.
- `@NotNull` does not stop a member being left `null` when a cycle is cut, or an element of
  `List<@NotNull Leaf>` at the [depth limit]({{ '/guides/object-generation/' | relative_url }}#depth-limit).
- Strict `LocalTime` and `MonthDay` constraints can have no representable value at a configured clock
  boundary; those cases fail rather than wrapping into the wrong temporal direction.
